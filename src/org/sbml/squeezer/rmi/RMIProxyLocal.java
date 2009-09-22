package org.sbml.squeezer.rmi;

/**
 * Title:        The JProxy Framework
 * Description:  API for distributed and parallel computing.
 * Copyright:    Copyright (c) 2004
 * Company:      University of Tuebingen
 * @version:  $Revision: 1.2 $
 *            $Date: 2004/04/15 12:28:34 $
 *            $Author: ulmerh $
 */
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 *
 */
public class RMIProxyLocal implements InvocationHandler, Serializable {
	/**
	 * Generated serial version uid.
	 */
	private static final long serialVersionUID = 3334945896167414037L;
	private RMIInvocationHandler m_RMIHandler;
	public static boolean TRACE = false;
	private Class<?> originalClass = null;

	/**
	 *
	 */
	public static Object newInstance(Object c, String RMIName) {
		if (TRACE)
			System.out.println("RMIProxyLocal:" + c.getClass().getName());
		RMIProxyLocal proxyLocal = new RMIProxyLocal(c, RMIName);
		Object ret = java.lang.reflect.Proxy.newProxyInstance(c.getClass()
				.getClassLoader(), c.getClass().getInterfaces(), proxyLocal);
		proxyLocal.setWrapper(ret);
		proxyLocal.setOriginalClass(c.getClass());
		if (TRACE)
			System.out.println(" --> " + ret.getClass());
		return ret;
	}

	/**
	 *
	 */
	public static Object newInstance(Object c) {
		if (TRACE)
			System.out.println("RMIProxyLocal:" + c.getClass().getName());
		RMIProxyLocal proxyLocal = new RMIProxyLocal(c);
		Object ret = java.lang.reflect.Proxy.newProxyInstance(c.getClass()
				.getClassLoader(), c.getClass().getInterfaces(), proxyLocal);
		proxyLocal.setWrapper(ret);
		proxyLocal.setOriginalClass(c.getClass());
		if (TRACE)
			System.out.println(" --> " + ret.getClass());
		return ret;
	}

	/**
	 *
	 */
	private RMIProxyLocal(Object c) {
		if (TRACE)
			System.out.println("RMIProxyLocal:" + c.getClass().getName());
		try {
			m_RMIHandler = new RMIInvocationHandlerImpl(c);
		} catch (Exception e) {
			System.err
					.println("Error in m_RMIHandler = new RMIInvokationHandlerImpl(c)");
			e.printStackTrace();
		}
	}

	/**
	 *
	 */
	private RMIProxyLocal(Object c, String RMIName) {
		try {
			m_RMIHandler = new RMIInvocationHandlerImpl(c, RMIName);
		} catch (Exception e) {
			System.out
					.println("Error in m_RMIHandler = new RMIInvokationHandlerImpl(c)");
			e.printStackTrace();
		}
	}

	/**
	 *
	 */
	public void setWrapper(Object Wrapper) {
		try {
			m_RMIHandler.setWrapper(Wrapper);
		} catch (Exception e) {
			System.out.println("Error in setWrapper " + e.getMessage());
		}
	}

	/**
	 *
	 */
	public Object invoke(Object proxy, Method m, Object[] args)
			throws Throwable {
		return m_RMIHandler.invoke(m.getName(), args);
	}

	/**
	 * @return the originalClass
	 */
	public Class<?> getOriginalClass() {
		return originalClass;
	}

	/**
	 * @param originalClass
	 *            the originalClass to set
	 */
	public void setOriginalClass(Class<?> originalClass) {
		if (TRACE)
			System.out.println("setting original proxy class "
					+ originalClass.getName());
		this.originalClass = originalClass;
	}
}
