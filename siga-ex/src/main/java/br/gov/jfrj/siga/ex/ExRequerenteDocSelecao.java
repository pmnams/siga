package br.gov.jfrj.siga.ex;

import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.hibernate.ExDao;
import br.gov.jfrj.siga.model.Selecao;

public class ExRequerenteDocSelecao extends Selecao<ExRequerenteDoc> {

    @Override
    public ExRequerenteDoc buscarObjeto() throws AplicacaoException {
        if (getId() == null)
            return null;

        return ExDao.getInstance().consultar(getId(), ExRequerenteDoc.class, false);
    }

    @Override
    public boolean buscarPorId() throws AplicacaoException {
        return buscarObjeto() != null;
    }

    @Override
    public boolean buscarPorSigla() throws AplicacaoException {
        return false;
    }


    @Override
    public String getAcaoBusca() {
        return "/requerente";
    }

}
