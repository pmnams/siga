<%@ tag body-content="scriptless" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://localhost/jeetags" prefix="siga" %>
<%@ taglib uri="http://localhost/libstag" prefix="f" %>
<%@ attribute name="titulo" %>
<%@ attribute name="popup" %>
<%@ attribute name="meta" %>
<%@ attribute name="pagina_de_erro" %>
<%@ attribute name="onLoad" %>
<%@ attribute name="desabilitarbusca" %>
<%@ attribute name="desabilitarmenu" %>
<%@ attribute name="iframe" %>
<%@ attribute name="incluirJs" %>
<%@ attribute name="compatibilidade" %>
<%@ attribute name="desabilitarComplementoHEAD" %>
<%@ attribute name="incluirBS" required="false" %>

<c:if test="${not empty pagina_de_erro}">
    <c:set var="pagina_de_erro" scope="request" value="${pagina_de_erro}"/>
</c:if>

<c:set var="siga_cliente" scope="request" value="${f:resource('/siga.local')}"/>

<c:set var="titulo_pagina" scope="request">${titulo}</c:set>

<c:if test="${iframe == 'sim'}">
    <c:if test="${empty incluirBS or incluirBS}">
        <link rel="stylesheet" href="/siga/bootstrap/css/bootstrap.min.css?v=4.1.1" type="text/css"
              media="screen, projection"/>
    </c:if>
</c:if>

<c:if test="${iframe != 'sim'}">
    <siga:cabecalho titulo="${titulo}" popup="${popup}" meta="${meta}"
                    onLoad="${onLoad}" desabilitarbusca="${desabilitarbusca}"
                    desabilitarmenu="${desabilitarmenu}" incluirJs="${incluirJs}"
                    compatibilidade="${compatibilidade}"
                    desabilitarComplementoHEAD="${desabilitarComplementoHEAD}"
                    incluirBS="${incluirBS}"/>
</c:if>

<jsp:doBody/>

<c:if test="${iframe != 'sim'}">
    <siga:rodape popup="${popup}" incluirJs="${incluirJs}" incluirBS="${incluirBS}"/>
</c:if>
