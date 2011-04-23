/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package controller;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author rdinarte
 */
public class RequestManager {

    // Error codes
    // -------------------------------------------------------------------------
    private enum ErrorCodes
    {
        InvalidCredentials,
        LoginServerError,
        ExperimentCreationError,
        ProgramCreationError
    }

    // Page names
    // -------------------------------------------------------------------------
    /**
     * The name of the login page
     */
    private static final String LoginPage = "/Hpca/login.jsp";
    /**
     * The name of the main menu page
     */
    private static final String MainMenuPage = "/Hpca/main.jsp";

    public static final String MyExperimentsPage = "/Hpca/normal/my-experiments.jsp";

    public static final String MyProgramsPage = "/Hpca/normal/my-programs.jsp";

    // Request parameters
    // -------------------------------------------------------------------------
    /**
     * The name of the request parameter in which errors will be reported
     */
    private static final String errorParam = "error";

    // Error messages
    // -------------------------------------------------------------------------
    /**
     * Error message reported to the user when he tries to login with invalid
     * credentials
     */
    private static final String invalidCredentialsMessage =
            "Nombre de usuario y/o contraseña incorrectos.";
    /**
     * Error message reported to the user when an unexpected error occurs while
     * trying to login
     */
    private static final String loginServerErrorMessage = "Se produjo un error al intentar "
            + "conectarse al sistema.<br />Por favor contacte al administrador dl sitio.";

    private static final String experimentCreationError = "Se produjo un error al intentar "
            + "crear el experimento.<br />Por favor contacte al administrador dl sitio.";

    // Class methods
    // -------------------------------------------------------------------------
    public static void Login(final HttpServletRequest request,
            final HttpServletResponse response, final int userId) throws IOException
    {
        SessionManager.Login(request, userId);
        response.sendRedirect(MainMenuPage);
    }

    public static boolean VerifyLogin(final HttpServletRequest request,
            final HttpServletResponse response) throws IOException
    {
        boolean result = SessionManager.IsLoggedIn(request);
        if(!result)
            response.sendRedirect(LoginPage);
        return result;
    }

    public static String GetError(final HttpServletRequest request)
    {
        Object requestError = request.getAttribute(errorParam);
        if(requestError == null)
            return "";
        ErrorCodes code = (ErrorCodes)requestError;
        switch (code)
        {
            case InvalidCredentials: return invalidCredentialsMessage;
            case LoginServerError: return loginServerErrorMessage;
            case ExperimentCreationError: return experimentCreationError;
            default: return "";
        }
    }

    public static void SendExperimentCreationError(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException, IOException
    {
        sendError(request, response, MyExperimentsPage, ErrorCodes.ExperimentCreationError);
    }

    public static void SendProgramCreationError(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException, IOException
    {
        sendError(request, response, MyProgramsPage, ErrorCodes.ProgramCreationError);
    }

    public static void SendInvalidCredentialsError(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException, IOException
    {
        sendError(request, response, LoginPage, ErrorCodes.InvalidCredentials);
    }

    public static void SendLoginServerError(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException, IOException
    {
        sendError(request, response, LoginPage, ErrorCodes.LoginServerError);
    }

    private static void sendError(final HttpServletRequest request,
            final HttpServletResponse response, final String path, final ErrorCodes errorCode)
            throws ServletException, IOException
    {
        RequestDispatcher dispatcher = request.getRequestDispatcher(path);
        request.setAttribute(errorParam, errorCode);
        dispatcher.forward(request, response);
    }

}
