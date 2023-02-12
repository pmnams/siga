<%@ page language="java" contentType="text/html; charset=UTF-8"
	buffer="64kb"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://localhost/jeetags" prefix="siga"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<script type="text/javascript">
function sbmt(page) {
	const search = new URLSearchParams(location.search);

	let pageId = (page / 10).toString();

	if(!search.get('page'))
		search.append('page', pageId)
	else
		search.set('page', pageId);
	location.search = search.toString();
}
function validarCPF(Objcpf){
	var strCPF = Objcpf.replace(".","").replace(".","").replace("-","").replace("/","");
    var Soma;
    var Resto;
    Soma = 0;
	
    for (i=1; i<=9; i++) Soma = Soma + parseInt(strCPF.substring(i-1, i)) * (11 - i);
    Resto = (Soma * 10) % 11;
	
    if ((Resto == 10) || (Resto == 11))  Resto = 0;
    if (Resto != parseInt(strCPF.substring(9, 10)) ) {
    	
    	alert('CPF Inválido!');
        return false;
	}
    Soma = 0;
    for (i = 1; i <= 10; i++) Soma = Soma + parseInt(strCPF.substring(i-1, i)) * (12 - i);
    Resto = (Soma * 10) % 11;

    if ((Resto == 10) || (Resto == 11))  Resto = 0;
    if (Resto != parseInt(strCPF.substring(10, 11) ) ) {
    	
    	alert('CPF Inválido!');
    	return false;
    }
    return true;
         
}
function cpf_mask(v){
	v=v.replace(/\D/g,"");
	v=v.replace(/(\d{3})(\d)/,"$1.$2");
	v=v.replace(/(\{3})(\d)/,"$1.$2");
	v=v.replace(/(\d{3})(\d{1,2})$/,"$1-$2");

	if(v.length == 14) {
    	validarCPF(v);
    }
	return v;
	}
</script>

<siga:pagina titulo="Listar Requerentes">
	<!-- main content -->
	<div class="container-fluid">
	<form name="frm" action="listar" class="form100" method="GET">
		<div class="card bg-light mb-3" >
			<div class="card-header">
				<h5>Dados do Requerente</h5>
			</div>
			<div class="card-body">
				<div class="row">
					<div class="col-md-4">
						<div class="form-group">
							<label for="nome">Nome</label>
							<input type="text" id="nome" name="nome" value="${nome}" maxlength="100" class="form-control"/>
						</div>					
					</div>					
					<div class="col-md-2">
						<div class="form-group">
							<label for="nome">CPF</label>
							<input type="text" id="cpfPesquisa" name="cpfPesquisa" value="${cpfPesquisa}" maxlength="14" onkeyup="this.value = cpf_mask(this.value)" class="form-control"/>
						</div>					
					</div>					
				</div>
				<div class="row">
					<div class="col-sm-2">
						<button type="submit" class="btn btn-primary">Pesquisar</button>
					</div>
				</div>				

			
			</div>
		</div>
	
		<h3 class="gt-table-head">Requerntes cadastrados</h3>
		<table border="0" class="table table-sm table-striped">
			<thead class="${thead_color}">
				<tr>
					<th align="left">Nome</th>
					<th align="left">CPF</th>
					<th align="left">Data de Nascimento</th>
					<th colspan="2" align="center">Op&ccedil;&otilde;es</th>					
				</tr>
			</thead>
			<tbody>
				<siga:paginador maxItens="10" maxIndices="10" totalItens="${totalItens}"
					itens="${itens}" var="requerente">
					<tr>
						<td align="left">${requerente.nomeRequerente}</td>
						<td align="left">${requerente.cpfFormatado}</td>
						<td align="left"><fmt:formatDate pattern = "dd/MM/yyyy" value = "${requerente.dataNascimento}" /></td>
						<td align="left">
							<c:url var="url" value="/app/requerente/editar">
								<c:param name="id" value="${requerente.id}"></c:param>
							</c:url>
							<a href="${url}" class="btn btn-primary" role="button" aria-pressed="true" >Editar</a>
							<c:url var="url" value="/app/requerente/remover">
								<c:param name="id" value="${requerente.id}"></c:param>
							</c:url>
							<a href="${url}" class="btn btn-danger" role="button" aria-pressed="true" >Remover</a>
						</td>							
					</tr>
				</siga:paginador>
			</tbody>
		</table>				
		<div class="gt-table-buttons">
				<c:url var="url" value="/app/requerente/editar"></c:url>
				<c:url var="urlAtivarInativar" value="/app/pessoa/ativarInativar"></c:url>
				<input type="button" value="Incluir" onclick="javascript:window.location.href='${url}'" class="btn btn-primary">
		</div>				
	</form>
	</div>

<script>
function carregarRelacionados(id) {
	frm.method = "POST";
	frm.action = 'carregarCombos';
	frm.submit();
	frm.method = "GET";
}
</script>
</siga:pagina>