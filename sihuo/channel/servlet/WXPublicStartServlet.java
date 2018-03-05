package com.channel.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

import cn.phoneposp.dao.SaruInfoDao;


public class WXPublicStartServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	Logger logger = Logger.getLogger(WXPublicStartServlet.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		this.doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=utf-8");
        logger.info("==============微信公众号扫码处理开始=============");
        SaruInfoDao dao = new SaruInfoDao();

        String sn = request.getParameter("sn");
        
        //判断二维码是否绑定商户
        String saruLruid = dao.getSarulruidBySn(sn);
        String ip = request.getRemoteAddr();
        if (!saruLruid.equals("")) {
        	String saruAllName = dao.getSaruAllName(saruLruid);
            request.setAttribute("saruLruid", saruLruid);
            request.setAttribute("saruAllName", saruAllName);
            request.setAttribute("ip", ip);
            //response.sendRedirect(request.getContextPath() + "/oneCodePay.jsp");
            request.getRequestDispatcher("wxPayInput.jsp").forward(request, response);
        } else {
            Map<String, String> map = new HashMap<String, String>();
            map.put("resultCode", "-99999");
            map.put("msg", "这个二维码没有绑定，请绑定！");
            Gson gson = new Gson();
            response.getWriter().write(gson.toJson(map));
            response.getWriter().flush();
            response.getWriter().close();
        }
        logger.info("==============微信公众号扫码处理结束=============");
		
	}

}
