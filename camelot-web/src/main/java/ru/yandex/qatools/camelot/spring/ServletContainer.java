package ru.yandex.qatools.camelot.spring;

import org.glassfish.jersey.servlet.WebConfig;
import ru.yandex.qatools.camelot.core.web.SpringServletFacade;

import javax.servlet.ServletException;

import static org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class ServletContainer extends org.glassfish.jersey.servlet.ServletContainer {

    @Override
    protected void init(WebConfig webConfig) throws ServletException {
        super.init(webConfig);

        // FIXME: pass spring servlet to the context
        SpringServletFacade facade = getWebApplicationContext(getServletContext()).getBean(SpringServletFacade.class);
        facade.setServletContainer(this);
    }
}
