package br.gov.jfrj.siga.api.v1;

import br.gov.jfrj.siga.api.v1.ISigaApiV1.Cargo;
import br.gov.jfrj.siga.api.v1.ISigaApiV1.ICargosGet;
import br.gov.jfrj.siga.base.util.Texto;
import br.gov.jfrj.siga.dp.DpCargo;
import br.gov.jfrj.siga.dp.dao.CpDao;
import br.gov.jfrj.siga.dp.dao.DpCargoDaoFiltro;
import com.crivano.swaggerservlet.SwaggerException;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class CargosGet implements ICargosGet {
    @Override
    public void run(Request req, Response resp, SigaApiV1Context ctx) throws Exception {
        if (StringUtils.isEmpty(req.idOrgao))
            throw new SwaggerException("O id do órgão é obrigatório.", 400, null, req, resp, null);

        resp.list = pesquisarPorNome(req);
        if (resp.list.isEmpty())
            throw new SwaggerException("Nenhum cargo foi encontrado para os parâmetros informados.", 404, null, req,
                    resp, null);
    }

    private List<Cargo> pesquisarPorNome(Request req) {
        final DpCargoDaoFiltro flt = new DpCargoDaoFiltro();
        if (!(req.nomeCargo == null || req.nomeCargo.isEmpty()))
            flt.setNome(Texto.removeAcentoMaiusculas(req.nomeCargo));
        flt.setIdOrgaoUsu(Long.valueOf(req.idOrgao));
        List<DpCargo> l = CpDao.getInstance().consultarPorFiltro(flt);
        return l.stream().map(this::cargoToResultadoPesquisa).collect(Collectors.toList());
    }

    @Override
    public String getContext() {
        return "selecionar cargos";
    }

    private Cargo cargoToResultadoPesquisa(DpCargo cargo) {
        Cargo crgo = new Cargo();

        crgo.sigla = (cargo.getSigla() != null ? cargo.getSigla() : null);
        crgo.idCargo = cargo.getId().toString();
        crgo.idCargoIni = cargo.getIdCargoIni().toString();
        crgo.idExterna = (cargo.getIdExterna() != null ? cargo.getIdExterna() : null);
        crgo.nome = (cargo.getNomeCargo() != null ? cargo.getNomeCargo() : null);
        return crgo;
    }

}
