<%@ page contentType="text/html; charset=UTF-8"
         buffer="64kb" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://localhost/jeetags" prefix="siga" %>

<%--@elvariable id="id" type="Long"--%>
<%--@elvariable id="nmOrgaoUsuario" type="String"--%>
<%--@elvariable id="siglaOrgaoUsuario" type="String"--%>
<%--@elvariable id="podeAlterarSigla" type="boolean"--%>
<%--@elvariable id="isExternoOrgaoUsu" type="Integer"--%>
<%--@elvariable id="dtContrato" type="String"--%>
<%--@elvariable id="hasBrasao" type="boolean"--%>
<%--@elvariable id="request" type="javax.servlet.http.HttpServletRequest"--%>

<siga:pagina titulo="Cadastro de Orgãos">

    <script>
        function validar() {
            const nmOrgaoUsuario = document.getElementsByName('nmOrgaoUsuario')[0].value;
            const siglaOrgaoUsuario = document.getElementsByName('siglaOrgaoUsuario')[0].value;
            const id = document.getElementsByName('id')[0].value;
            if (nmOrgaoUsuario == null || nmOrgaoUsuario === "") {
                sigaModal.alerta("Preencha o nome do Órgão.");
                document.getElementById('nmOrgaoUsuario').focus();
            } else {
                if (siglaOrgaoUsuario == null || siglaOrgaoUsuario === "") {
                    sigaModal.alerta("Preencha a sigla do Órgão.");
                    document.getElementById('siglaOrgaoUsuario').focus();
                } else {
                    if (id == null || id === "") {
                        sigaModal.alerta("Preencha ID do Órgão.");
                        document.getElementById('id').focus();
                    } else {
                        frm.submit();
                    }
                }

            }
        }

        function removeBrasao() {
            const imgContainer = document.querySelector('.image-container')
            const empty = document.querySelector('.empty')

            const imgInp = document.getElementById('imageFile')
            imgInp.value = ''

            imgContainer.style.display = 'none'
            empty.style.display = 'flex'



            document.querySelector('#removelBrasao').value = 'true'
        }


        function somenteLetras() {
            tecla = event.keyCode;
            return (tecla >= 65 && tecla <= 90) || (tecla >= 97 && tecla <= 122);
        }

        window.addEventListener("load", () => {
            const imgInp = document.getElementById('imageFile')

            imgInp.onchange = () => {
                const [file] = imgInp.files
                if (file) {
                    document.querySelector('.empty').style.display = 'none'
                    document.querySelector('.image-container').style.display = 'block'
                    document.getElementById('imageShow').src = URL.createObjectURL(file)
                }
            }
        });
    </script>

    <style>
        .empty {
            width: 200px;
            height: 200px;
            margin: 0 auto;
            font-size: 1.5em;
            display: flex;
            justify-content: center;
            align-items: center;
            text-align: center;
        }
    </style>

    <body>

    <main class="container-fluid">
        <form class="card bg-light mb-3" name="frm" action="${request.contextPath}/app/orgaoUsuario/gravar"
              method="POST" enctype="multipart/form-data">
            <input type="hidden" name="postback" value="1"/>
            <div class="card-header"><h5>Cadastro de Órgão Usuário</h5></div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-2 form-group">
                        <label for="id">ID</label>
                        <c:choose>
                            <c:when test="${empty id}">
                                <input type="text" id="id" name="id" value="${id}" maxlength="5" size="5"
                                       onkeydown="return verificaNumero(event);" class="form-control"/>
                                <input type="hidden" name="acao" value="i"/>
                            </c:when>
                            <c:otherwise>
                                <label class="form-control">${id}</label>
                                <input type="hidden" name="id" value="${id}"/>
                                <input type="hidden" name="acao" value="a"/>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="col-md-6 form-group">
                        <label for="nmOrgaoUsuario">Nome</label>
                        <input type="text" id="nmOrgaoUsuario" name="nmOrgaoUsuario" value="${nmOrgaoUsuario}"
                               maxlength="80" size="80" class="form-control"/>
                    </div>
                    <div class="col-md-4 form-group">
                        <label for="siglaOrgaoUsuario">Sigla</label>
                        <c:choose>
                            <c:when test="${empty siglaOrgaoUsuario || podeAlterarSigla}">
                                <input type="text" name="siglaOrgaoUsuario" id="siglaOrgaoUsuario"
                                       value="${siglaOrgaoUsuario}" maxlength="10" size="10"
                                       style="text-transform:uppercase"
                                       onkeydown="return somenteLetras(event);"
                                       onkeyup="this.value = this.value.trim()" class="form-control"/>
                            </c:when>
                            <c:otherwise>
                                <label class="form-control">${siglaOrgaoUsuario }</label>
                                <input type="hidden" name="siglaOrgaoUsuario" value="${siglaOrgaoUsuario}"/>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="col-md-2 form-group">
                        <label for="dtContrato">Data de Assinatura do Contrato</label>
                        <input type="text" id="dtContrato" name="dtContrato" value="${dtContrato}"
                               onblur="verifica_data(this,0);" class="form-control"/>
                    </div>
                    <div class="col-md-2">
                        <label>Tipo de Órgão</label>
                        <div class="form-check">
                            <input type="checkbox" class="form-check-input" id="isExternoOrgaoUsu"
                                   name="isExternoOrgaoUsu" value="1"
                                   <c:if test="${isExternoOrgaoUsu == 1}">checked</c:if> />
                            <label class="form-check-label" for="isExternoOrgaoUsu">Órgão com Acesso Externo</label>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-12 col-sm-8 col-xl-5 form-group">
                        <label for="imageFile" class="form-label">Brasão para modelos:</label>
                        <div class="empty border border-light rounded" style="display: ${hasBrasao? 'none': 'flex'}">
                            <span>Selecione uma imagem</span>
                        </div>

                        <input id="removelBrasao" type="hidden" name="removelBrasao">
                        <div class="image-container" style="display: ${hasBrasao? 'block': 'none'}">
                            <img id="imageShow"
                                 src="/siga/public/app/orgaoUsuario/${id}/brasao"
                                 class="rounded mx-auto d-block"
                                 alt="brasão" width="200px" height="200px"
                            >
                            <button
                                    class="btn btn-danger mt-2"
                                    type="button"
                                    style="margin: 0 auto; display: block;"
                                    onclick="removeBrasao()">
                                Remover
                            </button>
                        </div>

                        <div class="mt-4" style="font-size: unset">
                            <input class="form-control form-control-lg"
                                   id="imageFile"
                                   type="file"
                                   name="brasao"
                                   accept="image/png, image/jpeg"
                            >
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-sm-6 form-group">
                        <input type="button" value="Ok" onclick="validar();"
                               class="btn btn-primary"/>
                        <input type="button" value="Cancelar" onclick="history.back();"
                               class="btn btn-primary"/>
                    </div>
                </div>
            </div>
        </form>
    </main>
    </body>

</siga:pagina>