package br.gov.jfrj.siga.ex.api.v1;

import br.gov.jfrj.siga.ex.ExDocumento;
import br.gov.jfrj.siga.ex.ExMarca;
import br.gov.jfrj.siga.ex.ExMobil;
import br.gov.jfrj.siga.ex.api.v1.IExApiV1.IPainelListaGet;
import br.gov.jfrj.siga.ex.api.v1.IExApiV1.PainelListaItem;
import br.gov.jfrj.siga.hibernate.ExDao;

import java.util.ArrayList;
import java.util.List;

public class PainelListaGet implements IPainelListaGet {

    @Override
    public void run(Request req, Response resp, ExApiV1Context ctx) throws Exception {
        if (req.idMarcas == null || req.idMarcas.trim().isEmpty())
            return;
        String[] aMarcas = req.idMarcas.split(",");
        List<Long> lMarcas = new ArrayList<>();
        for (String s : aMarcas)
            lMarcas.add(Long.parseLong(s));
        List<Object[]> l = ExDao.getInstance().consultarPainelLista(lMarcas);

        for (Object[] o : l) {
            ExDocumento doc = (ExDocumento) o[0];
            ExMobil mobil = (ExMobil) o[1];
            ExMarca marca = (ExMarca) o[2];
            PainelListaItem r = new PainelListaItem();

            r.marcaId = marca.getIdMarca().toString();
            r.dataIni = marca.getDtIniMarca();
            r.dataFim = marca.getDtFimMarca();
            r.moduloId = marca.getCpTipoMarca().getIdTpMarca().toString();

            r.tipo = mobil.doc().getExFormaDocumento().getExTipoFormaDoc().getDescricao();
            r.codigo = mobil.getCodigoCompacto();
            r.sigla = mobil.getSigla();
            if (mobil.doc().getSubscritor() != null && mobil.doc().getSubscritor().getLotacao() != null)
                r.origem = mobil.doc().getSubscritor().getLotacao().getSigla();
            r.descricao = mobil.doc().getDescrCurta();
            r.ultimaAnotacao = mobil.getDnmUltimaAnotacao();

            resp.list.add(r);
        }
    }

    @Override
    public String getContext() {
        return "obter lista de documentos por marcador";
    }

}
