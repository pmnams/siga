<%@ page contentType="text/html; charset=UTF-8"
         buffer="64kb" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://localhost/jeetags" prefix="siga" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<script>
    function sbmt(page) {
        console.log(2)
        const search = new URLSearchParams(location.search);

        let pageId = (page / 10).toString();

        if(!search.get('page'))
            search.append('page', pageId)
        else
            search.set('page', pageId);
        location.search = search.toString();
    }

    const requerentes = {};
</script>

<siga:pagina titulo="Buscar Pessoa" popup="true">
    <!-- main content -->
    <main class="card-body" style="max-height: 100%">
        <div class="row">
            <span>Nome ou CPF/CNPJ</span>
            <div class="input-group mb-2">
                <input style="cursor: pointer" type="text" class="form-control" onkeyup="buscar(event)"
                       placeholder="Busca" id="ref">
                <div class="input-group-append">
                    <span class="input-group-text" onclick="buscar();">Buscar</span>
                </div>
            </div>
        </div>
        <div class="row">
            <table class="table">
                <thead>
                <tr>
                    <th scope="col">Nome</th>
                    <th scope="col">CPF/CNPJ</th>
                    <th scope="col">E-Mail</th>
                    <th scope="col">Telefone</th>
                </tr>
                </thead>
                <siga:paginador maxItens="10" maxIndices="10"
                                totalItens="${numResultados}" itens="${requerentes}"
                                var="requerente">
                    <c:set var="tipoReq" scope="session"
                           value="${requerente.tipoRequerente}"/>

                    <tr>
                        <script class="temp">
                            requerentes["${requerente.id}"] = {
                                id: "${requerente.id}",
                                nomeRequerente: "${requerente.nomeRequerente}",
                                email: "${requerente.emailRequerente}",
                                tipo: "${requerente.tipoRequerente}",
                                endereco: "${requerente.endereco}",
                                numEndereco: "${requerente.numeroEndereco}",
                                bairro: "${requerente.bairro}",
                                cidade: "${requerente.cidade}",
                                telefone: "${requerente.telefoneRequerente}",
                                cadastro: "${requerente.cpfRequerente}"
                            }
                            <c:if test="${tipoReq == '1'}">
                            requerentes["${requerente.id}"] = Object
                                .assign(requerentes["${requerente.id}"], {
                                    registro: "${requerente.rgRequerente}",
                                    nascimento: "${requerente.dtNascimentoFormatado}",
                                    nacionalidade: "${requerente.nacionalidadeRequerente}",
                                    estadoCivil: "${requerente.estadoCivil}",
                                    conjugue: "${requerente.nomeConjugue}"
                                });
                            </c:if>
                        </script>
                        <td><a
                                href="javascript: requerenteOnClick(requerentes['${requerente.id}']);">${requerente.nomeRequerente}</a>
                        </td>
                        <td>${requerente.cpfFormatado}</td>
                        <td>${requerente.emailRequerente}</td>
                        <td>${requerente.telefoneRequerente}</td>
                    </tr>
                </siga:paginador>
            </table>
        </div>
        <script>
            function buscar(event) {

                if (event && event.keyCode !== 13)
                    return null;

                let value = document.getElementById('ref').value;
                location.replace('/sigaex/app/requerente/buscar?ref=' + value);
            }

            function requerenteOnClick(requerente) {

                if (parent.setEntrevista)
                    parent.setEntrevista(requerente)
                else
                    parent.retorna_requerenteDoc(requerente.id, requerente.cadastro, requerente.nomeRequerente)

            }

        </script>
    </main>
</siga:pagina>
