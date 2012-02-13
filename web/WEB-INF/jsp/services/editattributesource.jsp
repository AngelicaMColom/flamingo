<%--
Copyright (C) 2011 B3Partners B.V.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="/WEB-INF/jsp/taglibs.jsp"%>

<stripes:layout-render name="/WEB-INF/jsp/templates/ext.jsp">
    <stripes:layout-component name="head">
        <title>Bewerk Attribuutbron</title>
    </stripes:layout-component>
    <stripes:layout-component name="body">
        <p>
            <stripes:errors/>
            <stripes:messages/>
        <p>
            <stripes:form beanclass="nl.b3p.viewer.admin.stripes.AttributeSourceActionBean">
                <c:choose>
                    <c:when test="${actionBean.context.eventName == 'edit'}"> 
                    <h1>Attribuutbron bewerken</h1>

                    <stripes:hidden name="featureSource" value="${actionBean.featureSource.id}"/>
                    <table>
                        <tr>
                            <td>Naam:</td>
                            <td><stripes:text name="name" value="${actionBean.featureSource.name}" maxlength="255" size="30"/></td>
                        </tr>
                        <tr>
                            <td>Bron URL:</td>
                            <td>
                                <stripes:text name="url" value="${actionBean.featureSource.url}" maxlength="255" size="30" disabled="true"/>
                            </td>
                        </tr>
                        <tr>
                            <td>Type:</td>
                            <td>
                                <stripes:select name="protocol" disabled="true">
                                    <stripes:option value="wfs">WFS</stripes:option>
                                    <stripes:option value="arcgis">ArcGIS Server</stripes:option>
                                    <stripes:option value="arcxml">ArcXml</stripes:option>
                                    <stripes:option value="jdbc">JDBC</stripes:option>
                                </stripes:select>
                            </td>
                        </tr>
                        <tr>
                            <td>Gebruikersnaam:</td>
                            <td><stripes:text name="username" value="${actionBean.featureSource.username}" maxlength="255" size="30"/></td>
                        </tr>
                        <tr>
                            <td>Wachtwoord:</td>
                            <td><stripes:text name="password" value="${actionBean.featureSource.password}" maxlength="255" size="30"/></td>
                        </tr>
                    </table>

                    <stripes:submit name="saveEdit" value="Opslaan"/>
                    <stripes:submit name="cancel" value="Annuleren"/>
                </c:when>
                <c:when test="${actionBean.context.eventName == 'newAttributeSource' || actionBean.context.eventName == 'save'}">
                    
                            <h1>Nieuwe attribuutbron toevoegen</h1>

                            <table>
                                <tr>
                                    <td>Naam:</td>
                                    <td><stripes:text name="name" maxlength="255" size="30"/></td>
                                </tr>
                                <tr>
                                    <td>Bron host:</td>
                                    <td>
                                        <stripes:text name="host" maxlength="255" size="30"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>Bron poort:</td>
                                    <td>
                                        <stripes:text name="port" maxlength="255" size="30"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>Type:</td>
                                    <td>
                                        <stripes:text name="protocol" value="jdbc" readonly="true"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>Database type:</td>
                                    <td>
                                        <stripes:select name="dbtype">
                                            <stripes:option value="oracle">Oracle</stripes:option>
                                            <stripes:option value="postgis">Postgis</stripes:option>
                                        </stripes:select>
                                    </td>
                                </tr>
                                <tr>
                                    <td>Database:</td>
                                    <td>
                                        <stripes:text name="database" maxlength="255" size="30"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>Schema:</td>
                                    <td>
                                        <stripes:text name="schema" maxlength="255" size="30"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>Gebruikersnaam:</td>
                                    <td><stripes:text name="username" maxlength="255" size="30"/></td>
                                </tr>
                                <tr>
                                    <td>Wachtwoord:</td>
                                    <td><stripes:text name="password" maxlength="255" size="30"/></td>
                                </tr>
                            </table>

                            <stripes:submit name="save" value="Opslaan"/>
                            <stripes:submit name="cancel" value="Annuleren"/>
                        
                </c:when>
                <c:when test="${actionBean.context.eventName == 'save' || actionBean.context.eventName == 'saveEdit'}">
                    <script type="text/javascript">
                        var frameParent = getParent();
                        if(frameParent && frameParent.reloadGrid) {
                            frameParent.reloadGrid();
                        }
                    </script>
                    <stripes:submit name="newAttributeSource" value="Nieuwe attribuutbron"/>
                </c:when>
                <c:when test="${actionBean.context.eventName == 'delete'}">
                    <script type="text/javascript">
                        var frameParent = getParent();
                        if(frameParent && frameParent.reloadGrid) {
                            frameParent.reloadGrid();
                        }
                    </script>
                    <stripes:submit name="newAttributeSource" value="Nieuwe attribuutbron"/>
                </c:when>
                <c:otherwise>
                    <script type="text/javascript">
                        var frameParent = getParent();
                        if(frameParent && frameParent.reloadGrid) {
                            frameParent.reloadGrid();
                        }
                    </script>
                    <stripes:submit name="newAttributeSource" value="Nieuwe attribuutbron"/>
                </c:otherwise>
            </c:choose>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>