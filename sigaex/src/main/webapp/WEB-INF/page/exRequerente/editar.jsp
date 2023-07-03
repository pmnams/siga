<%@ page language="java" contentType="text/html; charset=UTF-8"
	buffer="64kb"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://localhost/jeetags" prefix="siga"%>

<script type="text/javascript">
	function validar() {
		document.querySelector('#okButton').disabled = true;


		/*var nmRequerente = document.getElementsByName('nmRequerente')[0].value;	
		var dtNascimento = document.getElementsByName('dtNascimento')[0].value;
		var cpf = document.getElementsByName('cpf')[0].value;
		var email = document.getElementsByName('email')[0].value;
		var id = document.getElementsByName('id')[0].value;
		
		if (nmRequerente==null || nmRequerente=="") {
			mensagemAlerta("Preencha o nome do requerente.");
			document.getElementById('nmRequerente').focus();
			return;	
		}
		
		if(cpf==null || cpf == "") {
			mensagemAlerta("Preencha o CPF do requerente.");
			document.getElementById('cpf').focus();
			return;
		}

		if(email==null || email == "") {
			mensagemAlerta("Preencha o e-mail do requerente.");
			document.getElementById('email').focus();
			return;
		}

		if(!validarEmail(document.getElementsByName('email')[0])) {
			return;
		}

		if(dtNascimento != null && dtNascimento != "" && !data(dtNascimento)) {
			return;
		}

		if(!validarCPF(cpf)) {
			return;
		}*/
		frm.submit();
	}

	function mensagemAlerta(mensagem) {
		$('#alertaModal').find('.mensagem-Modal').text(mensagem);
		$('#alertaModal').modal();
	}

	function mascaraData(v) {
		v = v.replace(/\D/g, "");
		v = v.replace(/(\d{2})(\d)/, "$1/$2");
		v = v.replace(/(\d{2})(\d)/, "$1/$2");
		if (v.length == 10) {
			data(v);
		}

		return v;
	}

	function validarEmail(campo) {
		if (campo.value != "") {
			var RegExp = /\b[\w]+@[\w-]+\.[\w]+/;

			if (campo.value.search(RegExp) == -1) {
				mensagemAlerta("E-mail inválido!");
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	function data(campo) {
		var reg = /(([0-2]{1}[0-9]{1}|3[0-1]{1})\/(0[0-9]{1}|1[0-2]{1})\/[0-9]{4})/g; //valida dd/mm/aaaa
		if (campo.search(reg) == -1) {
			mensagemAlerta('Data inválida!');
			return false;
		}
		return true;
	}
	function validarCPF(Objcpf) {
		var strCPF = Objcpf.replace(".", "").replace(".", "").replace("-", "")
				.replace("/", "");
		var Soma;
		var Resto;
		Soma = 0;

		for (i = 1; i <= 9; i++)
			Soma = Soma + parseInt(strCPF.substring(i - 1, i)) * (11 - i);
		Resto = (Soma * 10) % 11;

		if ((Resto == 10) || (Resto == 11))
			Resto = 0;
		if (Resto != parseInt(strCPF.substring(9, 10))) {

			mensagemAlerta('CPF Inválido!');
			return false;
		}
		Soma = 0;
		for (i = 1; i <= 10; i++)
			Soma = Soma + parseInt(strCPF.substring(i - 1, i)) * (12 - i);
		Resto = (Soma * 10) % 11;

		if ((Resto == 10) || (Resto == 11))
			Resto = 0;
		if (Resto != parseInt(strCPF.substring(10, 11))) {

			mensagemAlerta('CPF Inválido!');
			return false;
		}
		return true;

	}
	function cpf_mask(v) {
		v = v.replace(/\D/g, "");
		v = v.replace(/(\d{3})(\d)/, "$1.$2");
		v = v.replace(/(\d{3})(\d)/, "$1.$2");
		v = v.replace(/(\d{3})(\d{1,2})$/, "$1-$2");

		if (v.length == 14) {
			validarCPF(v);
		}
		return v;
	}
	function validarCNPJ(cnpj) {

		cnpj = cnpj.replace(/[^\d]+/g, '');

		if (cnpj == '')
			return false;

		if (cnpj.length != 14)
			mensagemAlerta('CNPJ Inválido!');
		return false;

		// Elimina CNPJs invalidos conhecidos
		if (cnpj == "00000000000000" || cnpj == "11111111111111"
				|| cnpj == "22222222222222" || cnpj == "33333333333333"
				|| cnpj == "44444444444444" || cnpj == "55555555555555"
				|| cnpj == "66666666666666" || cnpj == "77777777777777"
				|| cnpj == "88888888888888" || cnpj == "99999999999999")
			return false;

		// Valida DVs
		tamanho = cnpj.length - 2
		numeros = cnpj.substring(0, tamanho);
		digitos = cnpj.substring(tamanho);
		soma = 0;
		pos = tamanho - 7;
		for (i = tamanho; i >= 1; i--) {
			soma += numeros.charAt(tamanho - i) * pos--;
			if (pos < 2)
				pos = 9;
		}
		resultado = soma % 11 < 2 ? 0 : 11 - soma % 11;
		if (resultado != digitos.charAt(0))
			mensagemAlerta('CNPJ Inválido!');
		return false;

		tamanho = tamanho + 1;
		numeros = cnpj.substring(0, tamanho);
		soma = 0;
		pos = tamanho - 7;
		for (i = tamanho; i >= 1; i--) {
			soma += numeros.charAt(tamanho - i) * pos--;
			if (pos < 2)
				pos = 9;
		}
		resultado = soma % 11 < 2 ? 0 : 11 - soma % 11;
		if (resultado != digitos.charAt(1))
			mensagemAlerta('CNPJ Inválido!');
		return false;

		return true;

	}

	function cnpj_mask(v) {
		v = v.replace(/\D/g, "");
		v = v.replace(/(\d{2})(\d)/, "$1.$2");
		v = v.replace(/(\d{3})(\d)/, "$1.$2");
		v = v.replace(/(\d{3})(\d{1,2})/, "$1/$2");
		v = v.replace(/(\d{3})(\d{1,2})$/, "$1-$2");

		if (v.length < 19)
			return v;
	}
	function tell_mask(v) {
		v = v.replace(/\D/g, "");
		v = v.replace(/(\d{1})/, "($1");
		v = v.replace(/(\(\d{2})/, "$1) ");
		v = v.replace(/(\d{5})(\d{1,4})/, "$1-$2");

		return v;
	}

	function validarNome(campo) {
		campo.value = campo.value.replace(
				/[^a-zA-ZáâãéêíóôõúçÁÂÃÉÊÍÓÔÕÚÇ'' ]/g, '');
	}

	function decideConjugue(v) {
	    let elStyle = document.getElementsByClassName('casado-only')[0].style;
	    if (v === '1') {
	      elStyle.display = 'flex';
	    } else if (elStyle.display === 'flex') {
	      elStyle.display = 'none'
	   }
	}
</script>

<siga:pagina titulo="Cadastro de Requerente">
	<!-- main content -->
	<div class="container-fluid">
		<div class="card bg-light mb-3">
			<div class="card-header">
				<h5>Dados d0 Requerente</h5>
			</div>
			<div class="card-body">
				<form name="frm"
					action="${request.contextPath}/app/requerente/gravar" method="POST">
					<input type="hidden" name="postback" value="1" /> <input
						type="hidden" name="id" value="${id}" />
					<div class="row">
						<div class="col-sm-4">
							<div class="form-group">
								<label for="nmRequerente">Nome</label> <input type="text"
									id="nmRequerente" name="nmRequerente" value="${nmRequerente}"
									maxlength="60" class="form-control" onkeyup="validarNome(this)" />
							</div>
						</div>
						<div class="col-sm-4">
							<div class="form-group">
								<label for="email">E-mail</label> <input type="text" id="email"
									name="email" value="${email}" maxlength="60"
									onchange="validarEmail(this)"
									onkeyup="this.value = this.value.toLowerCase().trim()"
									class="form-control" />
							</div>
						</div>
						<div class="col-sm-3">
							<div class="form-group">
								<span style="display: block; margin-bottom: 9px;">Tipo de
									Pessoa</span>
								<div class="form-check form-check-inline">
									<input class="form-check-input" type="radio"
										name="tipoRequerente" id="tipoRequerente1" value="1"
										<c:if test = "${tipoRequerente == 1}">
												checked="checked"
      									</c:if>>
									<label class="form-check-label" for="tipoRequerente1">Física</label>
								</div>
								<div class="form-check form-check-inline">
									<input class="form-check-input" type="radio"
										name="tipoRequerente" id="tipoRequerente2" value="2"
										<c:if test = "${tipoRequerente == 2}">
												checked="checked"
      									</c:if>>
									<label class="form-check-label" for="tipoRequerente2">Jurídica</label>
								</div>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-sm-4">
							<div class="form-group">
								<label for="endereco">Endereço</label> <input type="text"
									id="endereco" name="endereco" value="${endereco}"
									maxlength="60" class="form-control" />
							</div>
						</div>
						<div class="col-sm-2">
							<div class="form-group">
								<label for="numero">Número</label> <input type="text"
									id="numero" name="numero" value="${numero}" maxlength="60"
									class="form-control" />
							</div>
						</div>
						<div class="col-sm-3">
							<div class="form-group">
								<label for="bairoo">Bairro</label> <input type="text"
									id="bairro" name="bairro" value="${bairro}" maxlength="60"
									class="form-control" />
							</div>
						</div>
						<div class="col-sm-3">
							<div class="form-group">
								<label for="cidade">Cidade</label> <input type="text"
									id="cidade" name="cidade" value="${cidade}" maxlength="60"
									class="form-control" />
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-sm-2">
							<div class="form-group">
								<label for="contato">Telefone</label> <input type="text"
									id="contato" name="contato" value="${contato}" maxlength="15"
									onkeyup="this.value = tell_mask(this.value);"
									class="form-control" />
							</div>
						</div>
						<div class="col-sm-2">
							<div class="form-group">
								<label id="cdRequerenteLabel" for="cdRequerente">CPF</label> <input
									type="text" id="cdRequerente"
									onchange="validarCPF(this.value);"
									onkeyup="this.value = cpf_mask(this.value);"
									name="cdRequerente" value="${cdRequerente}" maxlength="14"
									class="form-control" />
							</div>
						</div>
						<div class="user-only col-sm-2">
							<div class="form-group">
								<label for="registro">RG</label> <input type="text"
									id="registro" name="registro" value="${registro}"
									class="form-control" />
							</div>
						</div>
						<div class="user-only col-sm-2">
							<div class="form-group">
								<label for="dtNascimento">Data de Nascimento</label> <input
									type="text" id="dtNascimento" name="dtNascimento"
									value="${dtNascimento}" maxlength="10"
									onkeyup="this.value = mascaraData( this.value )"
									class="form-control" />
							</div>
						</div>
						<div class="user-only col-sm-2">
							<div class="form-group">
								<label for="nacionalidade">Nacionalidade</label> <input
									type="text" id="nacionalidade" name="nacionalidade"
									value="${nacionalidade}" class="form-control" />
							</div>
						</div>
						<div class="user-only col-sm-2">
							<div class="form-group">
								<label for="estadoCivil">Estado Cívil</label> 
								<select name="estadoCivil" class="form-control" id="estadoCivil"
								onchange="decideConjugue(this.value)" >
									<option value="0"
										<c:if test = "${estadoCivil == 0}">
										selected="selected"
      								</c:if>>Solteiro</option>
									<option value="1"
										<c:if test = "${estadoCivil == 1}">
										selected="selected"
      								</c:if>>Casado</option>
									<option value="2"
										<c:if test = "${estadoCivil == 2}">
										selected="selected"
      								</c:if>>Separado</option>
									<option value="3"
										<c:if test = "${estadoCivil == 3}">
										selected="selected"
      								</c:if>>Divorciado</option>
									<option value="4"
										<c:if test = "${estadoCivil == 4}">
										selected="selected"
      								</c:if>>Viúvo</option>
								</select>
							</div>
						</div>
					</div>
					<div class="casado-only row" style="display: none">
						<div class="user-only  col-sm-4">
							<div class="form-group">
								<label for="nmConjugue">Nome do Conjugue</label> <input
									type="text" id="nmConjugue" name="nmConjugue"
									value="${nmConjugue}" maxlength="60" class="form-control" />
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-sm-2">
							<div class="form-group">
								<button id="okButton" type="button" onclick="javascript: validar();"
									class="btn btn-primary">Ok</button>
								<button type="button" onclick="javascript:history.back();"
									class="btn btn-primary">Cancelar</button>
							</div>
						</div>
					</div>
				</form>
				<!-- Modal -->
				<div class="modal fade" id="alertaModal" tabindex="-1" role="dialog"
					aria-labelledby="exampleModalLabel" aria-hidden="true">
					<div class="modal-dialog" role="document">
						<div class="modal-content">
							<div class="modal-header">
								<h5 class="modal-title" id="alertaModalLabel">Alerta</h5>
								<button type="button" class="close" data-dismiss="modal"
									aria-label="Fechar">
									<span aria-hidden="true">&times;</span>
								</button>
							</div>
							<div class="modal-body">
								<p class="mensagem-Modal"></p>
							</div>
							<div class="modal-footer">
								<button type="button" class="btn btn-primary"
									data-dismiss="modal">Fechar</button>
							</div>
						</div>
					</div>
				</div>
				<!--Fim Modal -->
			</div>
		</div>
	</div>
</siga:pagina>

<script>
	$('input[type=radio][name=tipoRequerente]').change(function() {
		const sheet = new CSSStyleSheet();
		let element = document.getElementById("cdRequerente");
		let label = document.getElementById("cdRequerenteLabel");
		if (this.value == '1') {
			sheet.replaceSync('.user-only {display: block}');
			label.innerHTML = "CPF";
			element.onkeyup = function() {
				this.value = cpf_mask(this.value);
			}
			element.onchange = function() {
				validarCPF(this.value);
			}
			element.maxLength = 14;
			element.value = "";
		} else if (this.value == '2') {
			sheet.replaceSync('.user-only {display: none}');
			label.innerHTML = "CNPJ";
			element.onkeyup = function() {
				this.value = cnpj_mask(this.value);
			}
			element.onchange = function() {
				validarCNPJ(this.value);
			}
			element.maxLength = 18;
			element.value = "";
		}
		document.adoptedStyleSheets = [ sheet ];
	});

	function voltar() {
		frm.action = 'listar';
		frm.submit();
	}

	function carregarRelacionados(id) {
		frm.action = 'carregarCombos';
		frm.submit();
	}
</script>
