package ru.yandex.qatools.camelot.common.builders;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface SchedulerBuilder {

    /**
     * Schedule all the triggers within aggregator
     */
    void schedule() throws Exception; //NOSONAR

    /**
     * Unschedule all the triggers within aggregator
     */
    void unschedule() throws Exception; //NOSONAR

    /**
     * Invoke all the jobs
     */
    void invokeJobs() throws Exception; //NOSONAR

    /**
     * Invoke the one single job
     * Returns true if a method was invoked or false otherwise
     */
    boolean invokeJob(String method) throws Exception; //NOSONAR

}
