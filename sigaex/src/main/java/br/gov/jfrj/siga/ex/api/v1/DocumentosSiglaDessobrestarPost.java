package br.gov.jfrj.siga.ex.api.v1;

import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExMobil;
import br.gov.jfrj.siga.ex.api.v1.IExApiV1.IDocumentosSiglaDessobrestarPost;
import br.gov.jfrj.siga.ex.bl.Ex;
import br.gov.jfrj.siga.ex.logic.ExPodeDessobrestar;
import br.gov.jfrj.siga.vraptor.Transacional;

@Transacional
public class DocumentosSiglaDessobrestarPost implements IDocumentosSiglaDessobrestarPost {

    @Override
    public void run(Request req, Response resp, ExApiV1Context ctx) throws Exception {
        DpPessoa cadastrante = ctx.getCadastrante();
        DpLotacao lotaCadastrante = cadastrante.getLotacao();
        DpLotacao lotaTitular = ctx.getLotaTitular();

        ExMobil mob = ctx.buscarEValidarMobil(req.sigla, req, resp, "Documento a Dessobrestar");

        Ex.getInstance().getComp()
                .afirmar(
                        "O documento " + mob.getSigla() + " não pode ser dessobrestado por "
                                + cadastrante.getSiglaCompleta() + "/" + lotaTitular.getSiglaCompleta(),
                        ExPodeDessobrestar.class, cadastrante, lotaTitular, mob);

        ctx.assertAcesso(mob, cadastrante, lotaTitular);

        Ex.getInstance().getBL().desobrestar(cadastrante, lotaCadastrante, mob, null, cadastrante);

        resp.sigla = mob.doc().getCodigo();
        resp.status = "OK";
    }

    @Override
    public String getContext() {
        return "Arquivar no Corrente";
    }

}
