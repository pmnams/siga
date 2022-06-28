<%@ page pageEncoding="UTF-8" session="false" buffer="64kb" %>
<%@ taglib uri="http://localhost/jeetags" prefix="siga"%>
<%@ taglib uri="http://localhost/libstag" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>

<!DOCTYPE html>
<siga:pagina titulo="Gerar Protocolo" popup="true">

	<%--@elvariable id="url" type="String"--%>
	<%--@elvariable id="sigla" type="String"--%>
	<%--@elvariable id="dataHora" type="String"--%>
	<%--@elvariable id="doc" type="br.gov.jfrj.siga.ex.ExDocumento"--%>
	<%--@elvariable id="protocolo" type="br.gov.jfrj.siga.vraptor.ExProtocolo"--%>

	<link rel="stylesheet" href="/siga/bootstrap/css/bootstrap.min.css"	type="text/css" media="screen, projection, print" />
	<style>
	   @media print { 
	       #btn { display:none; }
	       #bg {-webkit-print-color-adjust: exact;}
	       
	       
	   }
	</style>
	<!-- main content bootstrap -->
	<div class="card-body">
		<div class="row">	

			<div class="col-sm-12">
				<div class="text-center">
					<c:set var="brasao"  value="${f:resource('/siga.relat.brasao')}"/>
					<c:choose>
						<c:when test="${fn:startsWith(brasao, 'https:') or fn:startsWith(brasao, 'http:')}">
							<c:set var = "brasao_file" value="${brasao}"/>
						</c:when>
						<c:otherwise>
							<c:set var = "brasao_file" value="${pageContext.request.contextPath}/imagens/${brasao}"/>
						</c:otherwise>
					</c:choose>

					<img src="${brasao_file}" class="rounded float-left" width="80px" alt="Brasão"/>
					<h4><b>${f:resource('/siga.relat.titulo')}</b></h4>
					<h5>${doc.orgaoUsuario.descricao}</h5>
					<h5>${doc.lotacao.descricao }</h5>
				</div>
			</div>
		</div>
		<br>
		
		<div  style="font-size: 26px">
		<div class="row">
			<div class="col-sm-12">
				<div class="p-3 mb-2 bg-dark text-white text-center"  id="bg">
					<h4><b>Protocolo de Acompanhamento de Documento</b></h4>
				</div>
			</div>
		</div>
		<br>
		<br>
		<div class="row">
			<div class="col-sm-12">
				<div class="form-group text-center">
					<label>N&uacute;mero do Documento: <b>${sigla}</b></label>
				</div>
			</div>
		</div>
		<div class="row">
			<div class="col-sm-12">
				<div class="form-group text-center">
					<label>Código do Protocolo: <b>${protocolo.codigo}</b></label>
				</div>
			</div>
		</div>
		<div class="row">
			<div class="col-sm-12">
				<div class="form-group text-center">
					<label>Data/Hora: ${dataHora}</label>
				</div>
			</div>
		</div>
		<br>
		<br>
		<div class="row">
			<div class="col-sm-12">
				<div class="form-group text-center">
					<label><b>Aten&ccedil;&atilde;o: </b>Para consultar o andamento do seu documento acesse  </label>
					<br />
					<a href="${url}" target="_blank">${url}</a>
				</div>
			</div>
		</div>
		
		</div>
		<br>
		<br>
		<br />
		<div id="btn">
			<button type="button" class="btn btn-primary" onclick="document.body.offsetHeight; window.print();" >Imprimir</button>
		</div>	
	</div>
	
</siga:pagina>
