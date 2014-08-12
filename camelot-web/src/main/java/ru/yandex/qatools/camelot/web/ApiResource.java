package ru.yandex.qatools.camelot.web;

import com.sun.research.ws.wadl.Application;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.server.wadl.WadlApplicationContext;
import org.glassfish.jersey.server.wadl.internal.ApplicationDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

@Path("/api")
@Component
public class ApiResource extends BasicViewResource {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Context
    UriInfo uriInfo;

    @Context
    WadlApplicationContext wadlContext;

    private byte[] wadlHtmlRepresentation;
    private byte[] wadlXmlRepresentation;

    @Override
    public Object getTitle() {
        return "API";
    }

    public synchronized void emptyCache() {
        wadlXmlRepresentation = null;
        wadlHtmlRepresentation = null;
    }

    public String getDoc(@Context HttpServletRequest request) {
        try {
            if (wadlContext.isWadlGenerationEnabled()) {
                logger.info("UriInfo: " + uriInfo);
                final Object entity = getWadl(uriInfo, wadlContext);
                String res = IOUtils.toString((InputStream) entity);
                return res.replaceAll(serverUrl(request), "");
            }
        } catch (Exception e) {
            logger.error("Failed to render documentation", e);
            return "Could not render documentation: " + e.getMessage();
        }
        return "Could not render documentation: wadl is disabled";
    }


    private String serverUrl(HttpServletRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append(request.getScheme()).
                append("://").
                append(request.getServerName()).
                append(":").append(request.getServerPort());
        return builder.toString();
    }

    public synchronized ByteArrayInputStream getWadl(@Context UriInfo uriInfo,
                                                     @Context WadlApplicationContext wadlContext) {
        final ApplicationDescription applicationDescription = wadlContext.getApplication(uriInfo, false);
        final Application application = applicationDescription.getApplication();

        if ((wadlXmlRepresentation == null)) {
            try {
                final Marshaller marshaller = wadlContext.getJAXBContext().createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                final ByteArrayOutputStream os = new ByteArrayOutputStream();
                marshaller.marshal(application, os);
                wadlXmlRepresentation = os.toByteArray();
                os.close();
            } catch (Exception e) {
                throw new RuntimeException("Could not marshal wadl Application.", e);
            }
        }
        if (wadlHtmlRepresentation == null) {
            try {
                /*
                * Create a StreamSource for the wadl stylesheet.
                */
                String xslResource = "/wadl.xsl";
                URL xslUrl = this.getClass().getResource(xslResource);
                InputStream xslStream = xslUrl.openStream();
                StreamSource xslSource = new StreamSource(xslStream);

                /*
                * Get a Transformer using the stylesheet StreamSource
                */
                TransformerFactory tFactory = TransformerFactory.newInstance();
                Transformer transformer = tFactory.newTransformer(xslSource);

                /*
                * Create a StreamSource for the WADL XML byte array
                */
                InputStream xmlStream = new BufferedInputStream(
                        new ByteArrayInputStream(wadlXmlRepresentation)
                );
                StreamSource xmlSource = new StreamSource(xmlStream);

                /*
                * Perform the transform to an output stream
                */
                ByteArrayOutputStream htmlOs = new ByteArrayOutputStream();
                transformer.transform(xmlSource, new StreamResult(htmlOs));

                wadlHtmlRepresentation = htmlOs.toByteArray();

            } catch (Exception e) {
                throw new RuntimeException("Could not marshal wadl Application as HTML.", e);
            }
        }
        return new ByteArrayInputStream(wadlHtmlRepresentation);
    }

}
