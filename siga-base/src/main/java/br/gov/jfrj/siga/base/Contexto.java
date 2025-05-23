/*******************************************************************************
 * Copyright (c) 2006 - 2011 SJRJ.
 *
 *     This file is part of SIGA.
 *
 *     SIGA is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     SIGA is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with SIGA.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package br.gov.jfrj.siga.base;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;

public class Contexto {
    public static Object resource(String name) {
        Context initContext = null;
        Context envContext = null;

        try {
            return Prop.get(name);
        } catch (Exception ex) {
            try {
                initContext = new InitialContext();
                envContext = (Context) initContext.lookup("java:/comp/env");
                Object value = envContext.lookup(name);
                if (value != null)
                    return value;
            } catch (final NamingException e) {
            }
        }
        return null;
    }

    public static String urlBase(HttpServletRequest request) {
        String urlBase = Prop.get("/siga.external.base.url");

        if (urlBase == null || urlBase.trim().length() == 0)
            urlBase = request.getScheme() + "://"
                    + request.getServerName() + ":"
                    + request.getServerPort();
        return urlBase;
    }

    public static String internallUrlBase() {
        return Prop.get("/siga.internal.base.url");
    }
}
