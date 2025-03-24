package com.ttn.project.core.servlets;

import com.google.gson.JsonObject;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;


import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

@Component(
        service = Servlet.class,
        property = {
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/ttnProject",
                "sling.servlet.methods=POST"
        }
)
public class UpdatePageTitleServlet extends SlingAllMethodsServlet {



    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String pagePath = request.getParameter("pagePath");
        String newTitle = request.getParameter("newTitle");

        JsonObject jsonResponse = new JsonObject();


        // Correcting the path to target jcr:content
        String pageContentPath = pagePath + "/jcr:content";

        ResourceResolver resourceResolver = request.getResourceResolver();
        Resource pageContentResource = resourceResolver.getResource(pageContentPath);

        if (pageContentResource == null) {
            jsonResponse.addProperty("error", "Page content node not found: " + pageContentPath);
            response.getWriter().write(jsonResponse.toString());
            return;
        }

        try {
            ModifiableValueMap properties = pageContentResource.adaptTo(ModifiableValueMap.class);
            if (properties != null) {
                String preTitle= properties.get("jcr:title", String.class);
                properties.put("jcr:title", newTitle);
                // Save changes
                Session session = resourceResolver.adaptTo(Session.class);
                if (session != null) {
                    session.save();
                }

                jsonResponse.addProperty("success", "Page title updated successfully.");
                jsonResponse.addProperty("prevTitle", preTitle);
                jsonResponse.addProperty("newTitle", newTitle);
            } else {
                jsonResponse.addProperty("error", "Unable to modify the page content node.");
            }
        } catch (RepositoryException e) {
            jsonResponse.addProperty("error", "RepositoryException: " + e.getMessage());
        }

        response.getWriter().write(jsonResponse.toString());
    }
}