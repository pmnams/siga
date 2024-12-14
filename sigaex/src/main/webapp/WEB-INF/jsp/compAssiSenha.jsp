<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page isErrorPage="true" import="java.io.*" contentType="text/html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://localhost/jeetags" prefix="siga" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://localhost/libstag" prefix="f" %>

<siga:pagina titulo="Assinatura" desabilitarbusca="sim" desabilitarmenu="sim" desabilitarComplementoHEAD="sim">
    <c:set var="contextException" scope="request" value="${pageContext.request.serverName}"/>
    <div class="jumbotron pt-2" style='font-size: 10pt;'>
        <h4 class="display-5">Operação finalizada<span class="lead">&nbsp;(${contextException})</span></h4>
        <div class="alert alert-warning" role="alert">Nenhum download disponível. Essa é uma assinatura digital com
            login e senha do sistema SIGA
            amparado pelo decreto municipal 2.820/2021
        </div>
        <div class="card">
            <div class="card-header bg-dark text-white">
                Dados da Execução
            </div>
            <ul class="list-group list-group-flush">
                <li class="list-group-item">
                    &nbsp;<strong>Data/Hora</strong>
                    <jsp:useBean id="now" class="java.util.Date"/>
                    <fmt:formatDate var="datahora" value="${now}" pattern="dd/MM/yyyy HH:mm:ss"/>
                    <c:out value="${datahora}"/>
                </li>
                <li class="list-group-item">
                    <strong>Ambiente </strong>
                    <c:out value="${f:resource('/siga.ambiente')}"/>
                    &nbsp;<strong>Versão </strong>SIGA <c:out value="${siga_version}"/>
                </li>
            </ul>
            <div class="card-footer text-center pt-2">
                <input type="button" value="Voltar" onclick="javascript:history.back();"
                       class="btn btn-primary"/>
            </div>
        </div>
    </div>


</siga:pagina>