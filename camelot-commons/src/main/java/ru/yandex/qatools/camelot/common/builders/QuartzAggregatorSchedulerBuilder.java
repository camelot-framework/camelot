package ru.yandex.qatools.camelot.common.builders;

import org.apache.camel.CamelContext;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.api.annotations.OnTimer;
import ru.yandex.qatools.camelot.api.error.RepositoryUnreachableException;
import ru.yandex.qatools.camelot.common.AggregatorPluginAnnotatedMethodInvoker;
import ru.yandex.qatools.camelot.common.PluginAnnotatedMethodInvoker;
import ru.yandex.qatools.camelot.common.PluginMethodInvoker;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.config.PluginContext;
import ru.yandex.qatools.camelot.error.PluginsSystemException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import static java.lang.String.format;
import static java.lang.System.identityHashCode;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ru.yandex.qatools.camelot.util.ReflectUtil.*;
import static ru.yandex.qatools.camelot.util.ServiceUtil.forEachAnnotatedMethod;


/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class QuartzAggregatorSchedulerBuilder implements SchedulerBuilder {

    public static final String PROP_EXPR = "\\$\\{(.+)\\}";
    public static final String SKIP_IF_NOT_COMPLETED = "skipIfNotCompleted";
    public static final String INVOKER = "invoker";
    public static final String METHOD = "method";
    public static final String SCHEDULE = "schedule";
    public static final String BUILDER = "builder";
    public static final String CRON = "cron";
    public static final String CRON_METHOD = "cronMethod";
    public static final String READ_ONLY = "readOnly";
    public static final String PER_STATE = "perState";
    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzAggregatorSchedulerBuilder.class);
    private final CamelContext camelContext;
    private final Plugin plugin;
    private final Scheduler scheduler;
    private final String contextId;
    private final Map<String, JobDetail> jobs = new HashMap<>();
    private final Set<String> runningJobs = new ConcurrentSkipListSet<>();

    /**
     * Initialize the new scheduler builder
     */
    public QuartzAggregatorSchedulerBuilder(CamelContext camelContext, Scheduler scheduler, Plugin plugin) {
        this.camelContext = camelContext;
        this.plugin = plugin;
        this.scheduler = scheduler;
        // NB: this code assumes the different schedulers for each camel context
        this.contextId = String.valueOf(identityHashCode(camelContext));
        try {
            scanJobs();
        } catch (Exception e) {
            LOGGER.error("Failed to scan the schedule jobs for plugin {}", plugin.getId(), e);
        }
    }

    /**
     * Build the timers and attach them to the context
     */
    @SuppressWarnings("unchecked")
    private void scanJobs() throws Exception { //NOSONAR

        final Class<?> aggClass = plugin.getContext().getClassLoader()
                .loadClass(plugin.getContext().getPluginClass());
        forEachAnnotatedMethod(aggClass, OnTimer.class, (method, annotation) -> { //NOSONAR
            Object timerInfo;
            try {
                timerInfo = getAnnotation(method, OnTimer.class);
                final PluginContext context = plugin.getContext();
                final PluginMethodInvoker invoker;

                final Boolean readOnly = (Boolean) getAnnotationValue(timerInfo, READ_ONLY);
                final Boolean perState = (Boolean) getAnnotationValue(timerInfo, PER_STATE);
                if (perState) {
                    invoker = new AggregatorPluginAnnotatedMethodInvoker(
                            camelContext, plugin, OnTimer.class, readOnly
                    ).process();
                } else {
                    invoker = new PluginAnnotatedMethodInvoker(plugin, OnTimer.class).process();
                }
                if (timerInfo != null) {
                    addNewJob(method, timerInfo, context, invoker);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to process scheduler method {} of class {}",
                        method.getName(), aggClass, e);
            }
            return null;
        });
    }

    private void addNewJob(Method method, Object timerInfo, PluginContext context, PluginMethodInvoker invoker)
            throws ReflectiveOperationException {

        final String groupName = plugin.getId();
        final String jobName = groupName + "." + method.getName() + contextId;
        String schedule = (String) getAnnotationValue(timerInfo, CRON);
        String cronMethod = (String) getAnnotationValue(timerInfo, CRON_METHOD);
        if (schedule.matches(PROP_EXPR)) {
            schedule = context.getAppConfig().getProperty(schedule.replaceAll(PROP_EXPR, "$1"));
        } else if (isEmpty(schedule) && !isEmpty(cronMethod)) {
            try {
                final Class<?> pClazz = context.getClassLoader().loadClass(context.getPluginClass());
                final Object pluginObj = pClazz.newInstance();
                context.getInjector().inject(pluginObj, context, null);

                for (Method m : getMethodsInClassHierarchy(pClazz)) {
                    if (m.getName().equals(cronMethod)) { //NOSONAR
                        if (m.getParameterTypes().length == 1) {
                            schedule = (String) m.invoke(pluginObj, method.getName());
                        } else {
                            schedule = (String) m.invoke(pluginObj);
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                throw new PluginsSystemException(format(
                        "Failed to calculate cron expression using method %s of class %s",
                        cronMethod, context.getPluginClass()), e);
            }
        }
        JobDetail job = new JobDetail(jobName, groupName, ScheduledJob.class);
        job.getJobDataMap().put(BUILDER, this);
        job.getJobDataMap().put(METHOD, method);
        job.getJobDataMap().put(INVOKER, invoker);
        job.getJobDataMap().put(SCHEDULE, schedule);
        job.getJobDataMap().put(SKIP_IF_NOT_COMPLETED, getAnnotationValue(timerInfo, SKIP_IF_NOT_COMPLETED));
        jobs.put(method.getName(), job);
    }

    /**
     * Unschedule all the triggers within aggregator
     */
    @Override
    public void unschedule() throws Exception { //NOSONAR
        for (JobDetail job : jobs.values()) {
            scheduler.unscheduleJob(job.getName(), job.getGroup());
            scheduler.deleteJob(job.getName(), job.getGroup());
        }
    }

    @Override
    public void invokeJobs() throws Exception { //NOSONAR
        for (String method : jobs.keySet()) {
            invokeJob(method);
        }
    }

    @Override
    public boolean invokeJob(String methodName) throws Exception { //NOSONAR
        final JobDetail job = jobs.get(methodName);
        final JobDataMap data = job.getJobDataMap();
        final Method method = (Method) data.get(METHOD);
        final PluginMethodInvoker invoker = (PluginMethodInvoker) data.get(INVOKER);
        final Boolean skipIfNotCompleted = (Boolean) data.get(SKIP_IF_NOT_COMPLETED);
        if (runningJobs.contains(job.getName()) && skipIfNotCompleted) {
            LOGGER.info("Still waiting until previous {} is finished... skipping execution!",
                    job.getName());
            return false;
        }
        runningJobs.add(job.getName());
        try {
            invoker.invoke(method);
            return true;
        } catch (IllegalStateException e) {
            LOGGER.debug("Failed to invoke scheduler job {}, because {}",
                    job.getName(), e.getMessage(), e);
        } catch (RepositoryUnreachableException e) {
            LOGGER.warn("Failed to invoke scheduler job {}, because of {}: {}",
                    job.getName(), e.getClass().getCanonicalName(), e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.warn("Failed to invoke scheduler job {}, because {}",
                    job.getName(), e.getMessage(), e);
        } finally {
            runningJobs.remove(job.getName());
        }
        return false;
    }

    /**
     * Schedule all the triggers within aggregator
     */
    @Override
    public void schedule() throws Exception { //NOSONAR
        for (JobDetail job : jobs.values()) {
            scheduler.scheduleJob(job, new CronTrigger(
                    job.getName(),
                    job.getGroup(),
                    (String) job.getJobDataMap().get("schedule")
            ));
        }
    }

    public static class ScheduledJob implements Job {
        private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledJob.class);

        @Override
        public void execute(JobExecutionContext context) {
            final JobDetail job = context.getJobDetail();
            final JobDataMap data = job.getJobDataMap();
            final SchedulerBuilder builder = (SchedulerBuilder) data.get(BUILDER);
            final Method method = (Method) data.get(METHOD);
            try {
                builder.invokeJob(method.getName());
            } catch (RepositoryUnreachableException e) {
                LOGGER.warn("Failed to invoke scheduler job {}, because of {}: {}",
                        job.getName(), e.getClass().getCanonicalName(), e.getMessage(), e);
            } catch (Exception e) {
                LOGGER.warn("Failed to invoke scheduler job {}, because {}",
                        job.getName(), e.getMessage(), e);
            }
        }
    }

}
