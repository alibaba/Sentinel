/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * @(#)CommandMap.java	1.20 07/05/14
 */

package javax.activation;


/**
 * The CommandMap class provides an interface to a registry of
 * command objects available in the system.
 * Developers are expected to either use the CommandMap
 * implementation included with this package (MailcapCommandMap) or
 * develop their own. Note that some of the methods in this class are
 * abstract.
 */
public abstract class CommandMap {
    private static CommandMap defaultCommandMap = null;

    /**
     * Get the default CommandMap.
     * <p>
     *
     * <ul>
     * <li> In cases where a CommandMap instance has been previously set
     *      to some value (via <i>setDefaultCommandMap</i>)
     *  return the CommandMap.
     * <li>
     *  In cases where no CommandMap has been set, the CommandMap
     *       creates an instance of <code>MailcapCommandMap</code> and
     *       set that to the default, returning its value.
     *
     * </ul>
     *
     * @return the CommandMap
     */
    public static CommandMap getDefaultCommandMap() {
	if (defaultCommandMap == null)
	    defaultCommandMap = new MailcapCommandMap();

	return defaultCommandMap;
    }

    /**
     * Set the default CommandMap. Reset the CommandMap to the default by
     * calling this method with <code>null</code>.
     *
     * @param commandMap The new default CommandMap.
     * @exception SecurityException if the caller doesn't have permission
     *					to change the default
     */
    public static void setDefaultCommandMap(CommandMap commandMap) {
	SecurityManager security = System.getSecurityManager();
	if (security != null) {
	    try {
		// if it's ok with the SecurityManager, it's ok with me...
		security.checkSetFactory();
	    } catch (SecurityException ex) {
		// otherwise, we also allow it if this code and the
		// factory come from the same class loader (e.g.,
		// the JAF classes were loaded with the applet classes).
		if (CommandMap.class.getClassLoader() !=
			    commandMap.getClass().getClassLoader())
		    throw ex;
	    }
	}
	defaultCommandMap = commandMap;
    }

    /**
     * Get the preferred command list from a MIME Type. The actual semantics
     * are determined by the implementation of the CommandMap.
     *
     * @param mimeType	the MIME type
     * @return the CommandInfo classes that represent the command Beans.
     */
    abstract public  CommandInfo[] getPreferredCommands(String mimeType);

    /**
     * Get the preferred command list from a MIME Type. The actual semantics
     * are determined by the implementation of the CommandMap. <p>
     *
     * The <code>DataSource</code> provides extra information, such as
     * the file name, that a CommandMap implementation may use to further
     * refine the list of commands that are returned.  The implementation
     * in this class simply calls the <code>getPreferredCommands</code>
     * method that ignores this argument.
     *
     * @param mimeType	the MIME type
     * @param ds	a DataSource for the data
     * @return the CommandInfo classes that represent the command Beans.
     * @since	JAF 1.1
     */
    public CommandInfo[] getPreferredCommands(String mimeType, DataSource ds) {
	return getPreferredCommands(mimeType);
    }

    /**
     * Get all the available commands for this type. This method
     * should return all the possible commands for this MIME type.
     *
     * @param mimeType	the MIME type
     * @return the CommandInfo objects representing all the commands.
     */
    abstract public CommandInfo[] getAllCommands(String mimeType);

    /**
     * Get all the available commands for this type. This method
     * should return all the possible commands for this MIME type. <p>
     *
     * The <code>DataSource</code> provides extra information, such as
     * the file name, that a CommandMap implementation may use to further
     * refine the list of commands that are returned.  The implementation
     * in this class simply calls the <code>getAllCommands</code>
     * method that ignores this argument.
     *
     * @param mimeType	the MIME type
     * @param ds	a DataSource for the data
     * @return the CommandInfo objects representing all the commands.
     * @since	JAF 1.1
     */
    public CommandInfo[] getAllCommands(String mimeType, DataSource ds) {
	return getAllCommands(mimeType);
    }

    /**
     * Get the default command corresponding to the MIME type.
     *
     * @param mimeType	the MIME type
     * @param cmdName	the command name
     * @return the CommandInfo corresponding to the command.
     */
    abstract public CommandInfo getCommand(String mimeType, String cmdName);

    /**
     * Get the default command corresponding to the MIME type. <p>
     *
     * The <code>DataSource</code> provides extra information, such as
     * the file name, that a CommandMap implementation may use to further
     * refine the command that is chosen.  The implementation
     * in this class simply calls the <code>getCommand</code>
     * method that ignores this argument.
     *
     * @param mimeType	the MIME type
     * @param cmdName	the command name
     * @param ds	a DataSource for the data
     * @return the CommandInfo corresponding to the command.
     * @since	JAF 1.1
     */
    public CommandInfo getCommand(String mimeType, String cmdName,
				DataSource ds) {
	return getCommand(mimeType, cmdName);
    }

    /**
     * Locate a DataContentHandler that corresponds to the MIME type.
     * The mechanism and semantics for determining this are determined
     * by the implementation of the particular CommandMap.
     *
     * @param mimeType	the MIME type
     * @return		the DataContentHandler for the MIME type
     */
    abstract public DataContentHandler createDataContentHandler(String
								mimeType);

    /**
     * Locate a DataContentHandler that corresponds to the MIME type.
     * The mechanism and semantics for determining this are determined
     * by the implementation of the particular CommandMap. <p>
     *
     * The <code>DataSource</code> provides extra information, such as
     * the file name, that a CommandMap implementation may use to further
     * refine the choice of DataContentHandler.  The implementation
     * in this class simply calls the <code>createDataContentHandler</code>
     * method that ignores this argument.
     *
     * @param mimeType	the MIME type
     * @param ds	a DataSource for the data
     * @return		the DataContentHandler for the MIME type
     * @since	JAF 1.1
     */
    public DataContentHandler createDataContentHandler(String mimeType,
				DataSource ds) {
	return createDataContentHandler(mimeType);
    }

    /**
     * Get all the MIME types known to this command map.
     * If the command map doesn't support this operation,
     * null is returned.
     *
     * @return		array of MIME types as strings, or null if not supported
     * @since	JAF 1.1
     */
    public String[] getMimeTypes() {
	return null;
    }
}
