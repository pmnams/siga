<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" buffer="64kb" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="http://localhost/jeetags" prefix="siga" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%--@elvariable id="listaCossignatarios" type="java.util.List<br.gov.jfrj.siga.ex.ExMovimentacao>"--%>
<%--@elvariable id="mob" type="br.gov.jfrj.siga.ex.ExMobil"--%>
<%--@elvariable id="listaCossignatarios" type="java.util.List<br.gov.jfrj.siga.ex.ExMovimentacao>"--%>
<%--@elvariable id="siga_cliente" type="String"--%>
<%--@elvariable id="sigla" type="String"--%>
<%--@elvariable id="podeExibirArvoreDocsCossig" type="boolean"--%>
<%--@elvariable id="podeIncluirCossigArvoreDocs" type="boolean"--%>
<%--@elvariable id="funcaoCosignatario" type="String"--%>

<script type="javascript">
    function displayPersonalizar(thisElement) {
        const thatElement = document.getElementById('div_personalizacao');
        if (thisElement.checked) {
            thatElement.style.display = '';
        } else {
            document.getElementById('funcaoCosignatario').value = "";
            document.getElementById('unidadeCosignatario').value = "";
            thatElement.style.display = 'none';
        }
    }

    function validate() {
        sigaSpinner.mostrar();
        return true;
    }

    function incluirExcluirAcessoTempArvoreDocs(thisElement) {
        if (confirm('Ao clicar em OK você habilitará ou desabilitará o acesso ao documento completo para o(s) cossignatário(s). Deseja continuar?')) {
            var podeChamarServico = ${!listaCossignatarios.isEmpty()};
            if (podeChamarServico) {
                var incluirCossig = false;
                if (thisElement.checked) {
                    incluirCossig = true;
                }
                sigaSpinner.mostrar();
                //Chama servico caso ja exista cossigs presentes na table
                window.location.href = '${pageContext.request.contextPath}/app/expediente/mov/incluir_excluir_acesso_temp_arvore_docs?sigla=${sigla}&incluirCossig=' + incluirCossig;
            } else {
                //Hidden para dar submit dentro do form incluir_cosignatario_gravar
                document.getElementById('podeIncluirCossigArvoreDocs').value = thisElement.checked;
            }
        } else {
            thisElement.checked = !thisElement.checked;
        }
    }
</script>

<siga:pagina titulo="Movimentação">

    <c:if test="${not mob.doc.eletronico}">
        <script type="text/javascript">
            $("html").addClass("fisico");
            $("body").addClass("fisico");
        </script>
    </c:if>

    <!-- main content bootstrap -->
    <div class="container-fluid">
        <input type="hidden" id="sigaCliente" value="${siga_cliente}"/>
        <div class="card bg-light mb-3">
            <div class="card-header">
                <h5>
                    Inclusão de Cossignatário - ${mob.siglaEDescricaoCompleta}
                </h5>
            </div>
            <div class="card-body">
                <form action="incluir_cosignatario_gravar" namespace="/expediente/mov" cssClass="form" method="post"
                      onsubmit="return validate();">
                    <input type="hidden" name="postback" value="1"/>
                    <input type="hidden" name="sigla" value="${sigla}"/>
                    <input type="hidden" id="podeIncluirCossigArvoreDocs" name="podeIncluirCossigArvoreDocs"
                           value="${podeIncluirCossigArvoreDocs}"/>

                    <div class="row">
                        <div class="col-12 col-lg-6">
                            <siga:selecao titulo="Cossignatário" propriedade="cosignatario" modulo="siga"/>
                        </div>
                        <div class="col-12 col-lg-6">
                            <div class="form-group">
                                <label for="funcaoCosignatario" class="col-sm-2x col-form-label">Função; Lotação;
                                    Localidade</label>
                                <input class="form-control" type="text" id="funcaoCosignatario"
                                       name="funcaoCosignatario" size="50"
                                       value="${funcaoCosignatario}" maxlength="128"/>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-12 col-lg-6">
                            <input type="submit" value="Incluir" class="btn btn-primary"/>
                            <input type="button" value="Voltar" class="btn btn-secondary"
                                   onclick="location.href='${pageContext.request.contextPath}/app/expediente/doc/exibir?sigla=${sigla}';">
                        </div>
                    </div>
                </form>
            </div>
        </div>
        <h3 class="gt-table-head">Cossignatários adicionados</h3>
        <c:if test="${podeExibirArvoreDocsCossig}">
            <div class="row">
                <div class="col-xs-4">
                    <div class="form-group">
                        <div class="form-check form-check-inline mt-2">
                            <input type="checkbox" id="podeIncluirCossigArvoreDocsCheck"
                                   name="podeIncluirCossigArvoreDocsCheck"
                                   class="form-check-input ml-3"
                                   <c:if test="${podeIncluirCossigArvoreDocs}">checked</c:if>
                                   onchange="incluirExcluirAcessoTempArvoreDocs(this);"/>
                            <label class="form-check-label" for="podeIncluirCossigArvoreDocsCheck">Acessar Documento
                                Completo</label>
                            <a class="fas fa-info-circle text-secondary ml-1" data-toggle="tooltip" data-trigger="click"
                               data-placement="bottom"
                               title='Selecionar esse campo se houver a necessidade de permitir que o(s) cossignatário(s) acesse(m) o documento completo, enquanto o mesmo estiver pendente
															de assinatura. Atenção: Para habilitar ou desabilitar essa função, o documento deverá estar com status "Finalizado"'></a>
                        </div>
                        <div class="ml-3 mb-4">
                            <small class="form-text text-muted">Selecionar esse campo se houver a necessidade de
                                permitir que o(s) Cossignatário(s) acesse(m) o documento completo.</small>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>
        <div class="table-responsive">
            <table border="0" class="table table-sm table-striped">
                <thead class="thead-dark">
                <tr>
                    <th align="left" width="10%">Matrícula</th>
                    <th align="left">Nome</th>
                    <th align="left"><fmt:message key="usuario.lotacao"/></th>
                    <th align="left">Função</th>
                    <th align="left">Localidade</th>
                    <th align="left" width="5%">Excluir</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="mov" items="${listaCossignatarios}">
                    <tr>
                        <td>${mov.subscritor }</td>
                        <td>${mov.subscritor.nomePessoa }</td>
                        <td>${mov.nmLotacao }</td>
                        <td>${mov.nmFuncao }</td>
                        <td>${mov.nmLocalidade}</td>
                        <td><input type="button" value="Excluir"
                                   onclick="sigaSpinner.mostrar();location.href='${pageContext.request.contextPath}/app/expediente/mov/excluir?id=${mov.idMov}&redirectURL=/app/expediente/mov/incluir_cosignatario?sigla=${sigla}'"
                                   class="btn btn-danger"/>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</siga:pagina>
<script type="text/javascript">
    $('a[data-toggle="tooltip"]').tooltip({
        placement: 'bottom',
        trigger: 'click'
    });
</script>
