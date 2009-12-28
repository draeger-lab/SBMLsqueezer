/*
 *  SBMLsqueezer creates rate equations for reactions in SBML files
 *  (http://sbml.org).
 *  Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * This class loads other classes that implement certain interfaces or extend
 * certain super types. With this method it becomes possible to load and
 * initialize instances of certain classes at runtime.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @date 2009-09-22
 * @since 1.3
 */
public class Reflect {

	/**
	 * Compares the string values of given objects.
	 * 
	 * @author draeger
	 * 
	 * @param <T>
	 */
	static class ClassComparator<T> implements Comparator<T> {
		/**
		 * 
		 */
		public int compare(Object o1, Object o2) {
			return (o1.toString().compareTo(o2.toString()));
		}
	}

	/**
	 * 
	 */
	private static String[] dynCP = null;

	/**
	 * 
	 */
	static int missedJarsOnClassPath = 0;

	/**
	 * 
	 */
	private static boolean TRACE;

	/**
	 * 
	 */
	private static boolean useFilteredClassPath;

	/**
	 * 
	 * @param set
	 * @param cls
	 * @return
	 */
	private static int addClass(HashSet<Class<?>> set, Class<?> cls) {
		if (TRACE)
			System.out.println("adding class " + cls.getName());
		if (set.contains(cls)) {
			System.err.println("warning, Class " + cls.getName()
					+ " not added twice!");
			return 0;
		} else {
			set.add(cls);
			return 1;
		}
	}

	/**
	 * Collect all classes from a given package on the classpath. If includeSubs
	 * is true, the sub-packages are listed as well.
	 * 
	 * @param pckg
	 * @param includeSubs
	 * @param bSort
	 *            sort alphanumerically by class name
	 * @return An ArrayList of Class objects contained in the package which may
	 *         be empty if an error occurs.
	 */
	public static Class<?>[] getAllClassesInPackage(String pckg,
			boolean includeSubs, boolean bSort) {
		return getClassesInPackageFltr(new HashSet<Class<?>>(), pckg,
				includeSubs, bSort, null);
	}

	/**
	 * 
	 * @param pckg
	 * @param includeSubs
	 * @param bSort
	 * @param superClass
	 * @return
	 */
	public static Class<?>[] getAllClassesInPackage(String pckg,
			boolean includeSubs, boolean bSort, Class<?> superClass) {
		return getClassesInPackageFltr(new HashSet<Class<?>>(), pckg,
				includeSubs, bSort, superClass);
	}

	/**
	 * Retrieve assignable classes of the given package from classpath.
	 * 
	 * @param pckg
	 *            String denoting the package
	 * @param reqSuperCls
	 * @return
	 */
	public static Class<?>[] getAssignableClassesInPackage(String pckg,
			Class<?> reqSuperCls, boolean includeSubs, boolean bSort) {
		if (TRACE)
			System.out.println("requesting classes assignable from "
					+ reqSuperCls.getName());
		return getClassesInPackageFltr(new HashSet<Class<?>>(), pckg,
				includeSubs, bSort, reqSuperCls);
	}

	/**
	 * Return the names of all classes in the same package that are assignable
	 * from the named class, and that can be loaded through the classpath. If a
	 * class has a declared field called "hideFromGOE" this method will skip it.
	 * Abstract classes and interfaces will be skipped as well.
	 * 
	 * @see ReflectPackage.getAssignableClassesInPackage
	 * @param className
	 * @return
	 */
	public static ArrayList<String> getClassesFromClassPath(String className) {
		ArrayList<String> classes = new ArrayList<String>();
		int dotIndex = className.lastIndexOf('.');
		if (dotIndex <= 0) {
			System.err.println("warning: " + className + " is not a package!");
		} else {
			String pckg = className.substring(0, className.lastIndexOf('.'));
			Class<?>[] clsArr;
			try {
				clsArr = getAssignableClassesInPackage(pckg, Class
						.forName(className), true, true);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				clsArr = null;
			}
			if (clsArr == null) {
				System.err
						.println("Warning: No configuration property found for "
								+ className);
				classes.add(className);
			} else {
				for (Class<?> class1 : clsArr) {
					int m = class1.getModifiers();
					try {
						// a field allowing a class to indicate it doesn't want
						// to be displayed
						Field f = class1.getDeclaredField("hideFromGOE");
						if (f.getBoolean(class1) == true) {
							if (TRACE)
								System.out
										.println("Class "
												+ class1
												+ " wants to be hidden from GOE, skipping...");
							continue;
						}
					} catch (Exception e) {

					} catch (Error e) {
						System.err.println("Error on checking fields of "
								+ class1 + ": " + e);
						continue;
					}
					// if (f)
					if (!Modifier.isAbstract(m) && !class1.isInterface()) {
						/*
						 * don't take abstract classes or interfaces
						 */
						try {
							Class<?>[] params = new Class[0];
							class1.getConstructor(params);
							classes.add(class1.getName());
						} catch (NoSuchMethodException e) {
							System.err
									.println("GOE warning: Class "
											+ class1.getName()
											+ " has no default constructor, skipping...");
						}
					}
				}
			}
		}
		return classes;
	}

	/**
	 * 
	 * @param set
	 * @param directory
	 * @param pckgname
	 * @param includeSubs
	 * @param reqSuperCls
	 * @return
	 */
	public static int getClassesFromDirFltr(HashSet<Class<?>> set,
			File directory, String pckgname, boolean includeSubs,
			Class<?> reqSuperCls) {
		int cntAdded = 0;
		if (directory.exists()) {
			// Get the list of the files contained in the package
			String[] files = directory.list();
			for (int i = 0; i < files.length; i++) {
				// we are only interested in .class files
				if (files[i].endsWith(".class")) {
					// removes the .class extension
					try {
						Class<?> cls = Class.forName(pckgname + '.'
								+ files[i].substring(0, files[i].length() - 6));
						if (reqSuperCls != null) {
							if (reqSuperCls.isAssignableFrom(cls)) {
								cntAdded += addClass(set, cls);
							}
						} else {
							cntAdded += addClass(set, cls);
						}
					} catch (Exception e) {
						System.err
								.println("ReflectPackage: Couldnt get Class from jar for "
										+ pckgname
										+ '.'
										+ files[i]
										+ ": "
										+ e.getMessage());
					} catch (Error e) {
						System.err
								.println("ReflectPackage: Couldnt get Class from jar for "
										+ pckgname
										+ '.'
										+ files[i]
										+ ": "
										+ e.getMessage());
					}
				} else if (includeSubs) {
					// do a recursive search over subdirs
					File subDir = new File(directory.getAbsolutePath()
							+ File.separatorChar + files[i]);
					if (subDir.exists() && subDir.isDirectory()) {
						cntAdded += getClassesFromDirFltr(set, subDir, pckgname
								+ "." + files[i], includeSubs, reqSuperCls);
					}
				}
			}
		}
		return cntAdded;
	}

	/**
	 * Collect classes of a given package from the file system.
	 * 
	 * @param pckgname
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static int getClassesFromFilesFltr(HashSet<Class<?>> set,
			String path, String pckgname, boolean includeSubs,
			Class<?> reqSuperCls) {
		try {
			// Get a File object for the package
			File directory = null;
			String dir = null;
			try {
				ClassLoader cld = ClassLoader.getSystemClassLoader();
				if (cld == null) {
					throw new ClassNotFoundException("Can't get class loader.");
				}
				dir = path + "/" + pckgname.replace(".", "/");

				if (TRACE)
					System.out.println(".. opening " + path);

				directory = new File(dir);

			} catch (NullPointerException x) {
				if (TRACE) {
					System.err.println(directory.getPath() + " not found in "
							+ path);
					System.err.println("directory "
							+ (directory.exists() ? "exists" : "doesnt exist"));
				}
				return 0;
			}
			if (directory.exists()) {
				// Get the list of the files contained in the package
				return getClassesFromDirFltr(set, directory, pckgname,
						includeSubs, reqSuperCls);
			} else {
				if (TRACE)
					System.err.println(directory.getPath()
							+ " doesnt exist in " + path + ", dir was " + dir);
				return 0;
			}
		} catch (ClassNotFoundException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * Collect classes of a given package from a jar file.
	 * 
	 * @param jarName
	 * @param packageName
	 * @return
	 */
	public static int getClassesFromJarFltr(HashSet<Class<?>> set,
			String jarName, String packageName, boolean includeSubs,
			Class<?> reqSuperCls) {
		boolean isInSubPackage = true;
		int cntAdded = 0;

		packageName = packageName.replaceAll("\\.", "/");
		if (TRACE)
			System.out
					.println("Jar " + jarName + " looking for " + packageName);
		try {
			JarInputStream jarFile = new JarInputStream(new FileInputStream(
					jarName));
			JarEntry jarEntry;

			while ((jarEntry = jarFile.getNextJarEntry()) != null) {
				String jarEntryName = jarEntry.getName();
				// if (TRACE) System.out.println("- " + jarEntry.getName());
				if ((jarEntryName.startsWith(packageName))
						&& (jarEntryName.endsWith(".class"))) {
					// subpackages are hit here as well!
					if (!includeSubs) { // check if the class belongs to a
						// subpackage
						int lastDash = jarEntryName.lastIndexOf('/');
						if (lastDash > packageName.length() + 1)
							isInSubPackage = true;
						else
							isInSubPackage = false;
					}
					if (includeSubs || !isInSubPackage) { // take the right
						// ones
						String clsName = jarEntryName.replace("/", ".");
						try {
							// removes the .class extension
							Class<?> cls = Class.forName(clsName.substring(0,
									jarEntryName.length() - 6));
							if (reqSuperCls != null) {
								if (reqSuperCls.isAssignableFrom(cls)) {
									cntAdded += addClass(set, cls);
								}
							} else
								cntAdded += addClass(set, cls);
						} catch (Exception e) {
							System.err
									.println("ReflectPackage: Couldnt get Class from jar for "
											+ clsName + ": " + e.getMessage());
						} catch (Error e) {
							System.err
									.println("ReflectPackage: Couldnt get Class from jar for "
											+ clsName + ": " + e.getMessage());
						}
					}

					// classes.add (jarEntry.getName().replaceAll("/", "\\."));
				}
			}
		} catch (IOException e) {
			missedJarsOnClassPath++;
			if (missedJarsOnClassPath == 0) {
				System.err.println("Couldnt open jar from class path: "
						+ e.getMessage());
				System.err.println("Dirty class path?");
			} else if (missedJarsOnClassPath == 2)
				System.err
						.println("Couldnt open jar from class path more than once...");
			// e.printStackTrace();
		}
		return cntAdded;
	}

	/**
	 * Read the classes available for user selection from the properties or the
	 * classpath respectively
	 */
	public static ArrayList<String> getClassesFromProperties(String className) {
		if (TRACE)
			System.out
					.println("getClassesFromProperties - requesting className: "
							+ className);
		return getClassesFromClassPath(className);
	}

	/**
	 * Collect classes from a given package on the classpath which have the
	 * given Class as superclass or superinterface. If includeSubs is true, the
	 * sub-packages are listed as well.
	 * 
	 * @see Class.assignableFromClass(Class cls)
	 * @param pckg
	 * @return
	 */
	public static Class<?>[] getClassesInPackageFltr(HashSet<Class<?>> set,
			String pckg, boolean includeSubs, boolean bSort,
			Class<?> reqSuperCls) {
		String classPath = null;
		if (!useFilteredClassPath || (dynCP == null)) {
			classPath = System.getProperty("java.class.path", ".");
			if (useFilteredClassPath) {
				try {
					dynCP = getValidCPArray();
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			} else
				dynCP = getClassPathElements();
		}

		if (TRACE)
			System.out.println("classpath is " + classPath);
		for (int i = 0; i < dynCP.length; i++) {
			if (TRACE)
				System.out.println("reading element " + dynCP[i]);
			if (dynCP[i].endsWith(".jar")) {
				getClassesFromJarFltr(set, dynCP[i], pckg, includeSubs,
						reqSuperCls);
			} else {
				if (TRACE)
					System.out.println("reading from files: " + dynCP[i] + " "
							+ pckg);
				getClassesFromFilesFltr(set, dynCP[i], pckg, includeSubs,
						reqSuperCls);
			}
		}
		return hashSetToClassArray(set, bSort);
	}

	/**
	 * 
	 * @return
	 */
	public static String[] getClassPathElements() {
		String classPath = System.getProperty("java.class.path", ".");
		// System.out.println("classpath: " + classPath);
		return classPath.split(File.pathSeparator);
	}

	/**
	 * 
	 * @return
	 */
	public static String[] getValidCPArray() {
		ArrayList<String> valids = getValidCPEntries(getClassPathElements());
		// vp = valids.toArray(dynCP); // this causes Matlab to crash meanly.
		String[] vp = new String[valids.size()];
		for (int i = 0; i < valids.size(); i++)
			vp[i] = valids.get(i);
		return vp;
	}

	/**
	 * 
	 * @return
	 */
	public static ArrayList<String> getValidCPEntries(String[] pathElements) {
		// String[] pathElements = getClassPathElements();
		File f;
		ArrayList<String> valids = new ArrayList<String>(pathElements.length);
		for (int i = 0; i < pathElements.length; i++) {
			// System.err.println(pathElements[i]);
			f = new File(pathElements[i]);
			// if (f.canRead()) {valids.add(pathElements[i]);}
			if (f.exists() && f.canRead()) {
				valids.add(pathElements[i]);
			}
		}
		return valids;
	}

	public static Class<?>[] hashSetToClassArray(HashSet<Class<?>> set,
			boolean bSort) {
		Object[] clsArr = set.toArray();
		if (bSort) {
			Arrays.sort(clsArr, new ClassComparator<Object>());
		}

		List<Object> list;
		list = Arrays.asList(clsArr);
		return list.toArray(new Class[list.size()]);
	}

	/**
	 * 
	 * 
	 */
	public Reflect() {
		TRACE = true;
	}
}
