import com.mysql.cj.Session;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("LoginFilter: " + httpRequest.getRequestURI());

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

//        // Redirect to login page if the "user" attribute doesn't exist in session
//        HttpSession session = httpRequest.getSession();
//
//        // if it is an employee link and employee is not set
//        if (session.getAttribute("employee") == null && httpRequest.getRequestURI().equals("_dashboard")) {
//            httpResponse.sendRedirect("employee-login.html");
//        }
//        // if it is a user link and user is not set
//        else if (session.getAttribute("user") != null && !httpRequest.getRequestURI().equals("_dashboard")) {
//            httpResponse.sendRedirect("login.html");
//        }
//        else {
//            chain.doFilter(request, response);
//        }

        HttpSession session = httpRequest.getSession();

        if (httpRequest.getRequestURI().contains("dashboard") || 
           httpRequest.getRequestURI().contains("metadata") ||
            httpRequest.getRequestURI().contains("add") ) {
            if (session.getAttribute("employee") != null) {
                chain.doFilter(request, response);
            }
            else {
                httpResponse.sendRedirect("employee-login.html");
            }
        }
        else {
            if (session.getAttribute("user") != null) {
                chain.doFilter(request, response);
            }
//            else if (session.getAttribute("employee") != null) {
//                chain.doFilter(request, response);
//            }
            else {
                httpResponse.sendRedirect("login.html");
            }
        }

//        if (session.getAttribute("employee") == null &&
//                session.getAttribute("user") == null &&
//                httpRequest.getRequestURI().contains("_dashboard")) {
//
//            // if neither employee nor user is set, set dashboard queries to employee login
//            httpResponse.sendRedirect("employee-login.html");
//        }
//        else if (session.getAttribute("employee") != null &&
//                session.getAttribute("user") == null &&
//                httpRequest.getRequestURI().contains("_dashboard")) {
//
//            // if employee is set and its a dashboard query, let it through
//            chain.doFilter(request, response);
//        }
//        else if (session.getAttribute("employee") == null &&
//                session.getAttribute("user") == null &&
//                httpRequest.getRequestURI().contains("_dashboard")) {
//
//        }




//        if (httpRequest.getSession().getAttribute("user") == null) {
//
//            if (httpRequest.getRequestURI().contains("_dashboard") && httpRequest.getSession().getAttribute("employee") == null) {
//                httpResponse.sendRedirect("employee-login.html");
//            }
//            else {
//                httpResponse.sendRedirect("login.html");
//            }
//        }
//        else {
//            chain.doFilter(request, response);
//        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        System.out.println("inside LoginFilter init");

        allowedURIs.add("_dashboard/employee-login.html");
        allowedURIs.add("_dashboard/employee-login.js");
        allowedURIs.add("_dashboard/employee-login.css");
        allowedURIs.add("api/employee-login");

        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("login.css");
        allowedURIs.add("api/login");
    }

    public void destroy() {
        // ignored.
    }

}
