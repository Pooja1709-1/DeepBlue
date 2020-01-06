package com.acn.ebx;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.onwbp.base.repository.ModulesRegister;

public class RegistrationServlet extends HttpServlet {
	private static final long serialVersionUID = -7405920306505522247L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		ModulesRegister.registerWebApp(this, config);
	}

	@Override
	public void destroy() {
		ModulesRegister.unregisterWebApp(this, this.getServletConfig());
	}
}