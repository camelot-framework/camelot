package ru.yandex.qatools.camelot.web.core;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * @author Ilya Sadykov
 */
public class AtmosphereServlet extends org.atmosphere.cpr.AtmosphereServlet { //NOSONAR

    public static final String FRAMEWORK_ATTR = "AtmosphereFrameworkAttribute";

    @Override
    public void init(ServletConfig sc) throws ServletException {
        super.init(sc);
        sc.getServletContext().setAttribute(FRAMEWORK_ATTR, framework());
    }
}
