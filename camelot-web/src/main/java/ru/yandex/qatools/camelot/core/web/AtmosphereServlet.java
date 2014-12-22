package ru.yandex.qatools.camelot.core.web;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * @author Ilya Sadykov
 */
public class AtmosphereServlet extends org.atmosphere.cpr.AtmosphereServlet {

    public static final String FRAMEWORK_ATTR = "AtmosphereFrameworkAttribute";

    @Override
    public void init(ServletConfig sc) throws ServletException {
        super.init(sc);
        sc.getServletContext().setAttribute(FRAMEWORK_ATTR, framework);
    }
}
