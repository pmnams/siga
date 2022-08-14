<%@ page pageEncoding="UTF-8" session="false" buffer="64kb" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://localhost/customtag" prefix="tags" %>
<%@ taglib uri="http://localhost/jeetags" prefix="siga" %>
<%@ taglib uri="http://localhost/functiontag" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>
<siga:pagina titulo="Anexação de Arquivo Auxiliar">

    <%--@elvariable id="mob" type="br.gov.jfrj.siga.vraptor.ExMobil"--%>
    <%--@elvariable id="sigla" type="String"--%>

    <c:set var="extensoesInvalidas" scope="session" value="${f:resource('arquivosAuxiliares.extensoes.excecao')}" />

    <c:if test="${not mob.doc.eletronico}">
        <script type="text/javascript">
            $("html").addClass("fisico");
            $("body").addClass("fisico");
        </script>
    </c:if>

    <!-- main content bootstrap -->
    <div class="container-fluid">
        <div class="card bg-light mb-3">
            <div class="card-header">
                <h5>
                    Anexação de Arquivo Auxiliar - ${mob.siglaEDescricaoCompleta}
                </h5>
            </div>
            <div class="card-body">
                <form action="anexar_arquivo_auxiliar_gravar" method="post" onsubmit="sbmt.disabled=true;"
                      enctype="multipart/form-data" class="form">
                    <input type="hidden" name="postback" value="1"/>
                    <input type="hidden" name="sigla" value="${sigla}"/>
                    <p class="alert alert-warning"><strong>Atenção!</strong> <fmt:message
                            key="documento.arquivoAuxiliar.texto"/></p>
                    <div class="row">
                        <div class="col-sm-6">
                            <div class="form-control custom-file">
                                <input class="custom-file-input" id="idSelecaoArquivo" type="file" name="arquivo"
                                       accept="*.*" onchange="testTamanho()"/>
                                <label class="custom-file-label text-truncate" for="idSelecaoArquivo"
                                       data-browse="Escolha o Arquivo">Clique para selecionar o arquivo a anexar</label>
                            </div>
                        </div>
                    </div>
                    <div class="row mt-4">
                        <div class="col-sm">
                            <input type="submit" value="Ok" class="btn btn-primary"
                                   onclick="return validaSelecaoAnexo( this.form );" name="sbmt"/>
                            <input type="button" value="Voltar"
                                   onclick="window.location.href='/sigaex/app/expediente/doc/exibir?sigla=${sigla}'"
                                   class="btn btn-cancel ml-2"/>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <script>
        /**
         * Valida se o anexo foi selecionado ao clicar em OK
         */
        function validaSelecaoAnexo(form) {
            let result = true;
            const arquivo = form.arquivo;
            const fileExtension = arquivo.value.substring(arquivo.value.lastIndexOf("."));

            if (arquivo.value === '') {
                alert("O arquivo a ser anexado não foi selecionado!");
                result = false;
            }

            let extensoesInvalidas = "${extensoesInvalidas}".split(",");
            for (let extensaoInvalida of extensoesInvalidas) {
                if (fileExtension === extensaoInvalida || fileExtension === extensaoInvalida.toUpperCase()) {
                    alert("Extensão " + fileExtension + " inválida para inclusão do arquivo.");
                    result = false;
                    break;
                }
            }

            if (result) {
                result = testTamanho();
            }

            return result;
        }

        function testTamanho() {
            const tamanhoArquivo = parseInt(document.getElementById("idSelecaoArquivo").files[0].size);
            if (tamanhoArquivo > 10485760) {
                alert("TAMANHO DO ARQUIVO EXCEDE O PERMITIDO (10MB)!");
                return false;
            }
        }
    </script>

    <script>
        $('.custom-file-input').on('change', function () {
            let fileName = $(this).val().split('\\').pop();
            $(this).next('.custom-file-label').addClass("selected").html(fileName);
        });
    </script>
</siga:pagina>
