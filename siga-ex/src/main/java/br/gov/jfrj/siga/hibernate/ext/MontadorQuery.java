/*-*****************************************************************************
 * Copyright (c) 2006 - 2011 SJRJ.
 *
 *     This file is part of SIGA.
 *
 *     SIGA is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     SIGA is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with SIGA.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package br.gov.jfrj.siga.hibernate.ext;

import br.gov.jfrj.siga.cp.model.enm.CpMarcadorEnum;
import br.gov.jfrj.siga.hibernate.query.ext.IExMobilDaoFiltro;
import br.gov.jfrj.siga.hibernate.query.ext.IMontadorQuery;

public class MontadorQuery implements IMontadorQuery {

    public String montaQueryConsultaporFiltro(final IExMobilDaoFiltro flt, boolean apenasCount) {

        StringBuilder sbf = new StringBuilder();

        if (apenasCount)
            sbf.append("select count(1) from ExMarca label  inner join label.cpMarcador marcador inner join label.exMobil mob inner join mob.exDocumento doc");
        else
            sbf.append("select label.idMarca from ExMarca label inner join label.cpMarcador marcador inner join label.exMobil mob inner join mob.exDocumento doc");

        if (flt.getIdMod() != null && flt.getIdMod() != 0) {
            sbf.append(" INNER JOIN doc.exModelo exMod ");
        }

        if (flt.getUltMovIdEstadoDoc() != null && flt.getUltMovIdEstadoDoc() != 0) {
            sbf.append(" LEFT JOIN label.exMovimentacao mov ");
        }

        sbf.append(" where");

        if (flt.getUltMovIdEstadoDoc() != null && flt.getUltMovIdEstadoDoc() != 0) {
            sbf.append(" and marcador.hisIdIni = :idMarcadorIni");
            sbf.append(" and (dt_ini_marca is null or dt_ini_marca < :dbDatetime)");
            sbf.append(" and (dt_fim_marca is null or dt_fim_marca > :dbDatetime)");
            sbf.append(" AND (mov IS NULL OR mov.dtParam1 IS NULL OR mov.dtParam1 <= CURRENT_DATE)");
        } else {
            sbf.append(" and marcador.listavelPesquisaDefault = 1");
        }

        if (flt.getUltMovRespSelId() != null && flt.getUltMovRespSelId() != 0) {
            sbf.append(" and label.dpPessoaIni.idPessoa = :ultMovRespSelId");
        }

        if (flt.getUltMovLotaRespSelId() != null
                && flt.getUltMovLotaRespSelId() != 0) {
            sbf.append(" and label.dpLotacaoIni.idLotacao = :ultMovLotaRespSelId");
        }

        if (flt.getIdTipoMobil() != null && flt.getIdTipoMobil() != 0) {
            sbf.append(" and mob.exTipoMobil.idTipoMobil = :idTipoMobil");
        }

        if (flt.getNumSequencia() != null && flt.getNumSequencia() != 0) {
            sbf.append(" and mob.numSequencia = :numSequencia ");
        }

        if (flt.getIdOrgaoUsu() != null && flt.getIdOrgaoUsu() != 0) {
            sbf.append(" and doc.orgaoUsuario.idOrgaoUsu = :idOrgaoUsu");
        }

        if (flt.getAnoEmissao() != null && flt.getAnoEmissao() != 0) {
            sbf.append(" and doc.anoEmissao = :anoEmissao");
        }

        if (flt.getNumExpediente() != null && flt.getNumExpediente() != 0) {
            sbf.append(" and doc.numExpediente = :numExpediente");
        }

        if (flt.getIdTpDoc() != null && flt.getIdTpDoc() != 0) {
            sbf.append(" and doc.exTipoDocumento.idTpDoc = :idTpDoc");
        }

        if (flt.getIdFormaDoc() != null && flt.getIdFormaDoc() != 0) {
            sbf.append(" and doc.exFormaDocumento.idFormaDoc = :idFormaDoc");
        }

        if (flt.getIdTipoFormaDoc() != null && flt.getIdTipoFormaDoc() != 0) {
            sbf.append(" and doc.exFormaDocumento.exTipoFormaDoc.idTipoFormaDoc = :idTipoFormaDoc");
        }

        if (flt.getClassificacaoSelId() != null
                && flt.getClassificacaoSelId() != 0) {
            sbf.append(" and doc.exClassificacao.hisIdIni = :classificacaoSelId");
        }

        if (flt.getDescrDocumento() != null && !flt.getDescrDocumento().trim().isEmpty() && flt.getListaIdDoc() == null) {
            sbf.append(" and doc.descrDocumentoAI like :descrDocumento");
        }

        if (flt.getDtDoc() != null) {
            if (((Long) CpMarcadorEnum.EM_ELABORACAO.getId()).equals(flt.getUltMovIdEstadoDoc())) {
                sbf.append(" and doc.dtRegDoc >= ");
            } else {
                sbf.append(" and doc.dtDoc >= ");
            }
            sbf.append(":dtDoc");
        }

        if (flt.getDtDocFinal() != null) {
            if (((Long) CpMarcadorEnum.EM_ELABORACAO.getId()).equals(flt.getUltMovIdEstadoDoc())) {
                sbf.append(" and doc.dtRegDoc <= ");
            } else {
                sbf.append(" and doc.dtDoc <= ");
            }
            sbf.append(":dtDocFinal");
        }

        if (flt.getNumAntigoDoc() != null
                && !flt.getNumAntigoDoc().trim().isEmpty()) {
            sbf.append(" and upper(doc.numAntigoDoc) like :numAntigoDoc");
        }

        if (flt.getDestinatarioSelId() != null
                && flt.getDestinatarioSelId() != 0) {
            sbf.append(" and doc.destinatario.idPessoaIni = :destinatarioSelId");
        }

        if (flt.getLotacaoDestinatarioSelId() != null
                && flt.getLotacaoDestinatarioSelId() != 0) {
            sbf.append(" and doc.lotaDestinatario.idLotacaoIni = :lotacaoDestinatarioSelId");
        }

        if (flt.getNmDestinatario() != null
                && !flt.getNmDestinatario().trim().isEmpty()) {
            sbf.append(" and upper(doc.nmDestinatario) like :nmDestinatario");
        }

        if (flt.getOrgaoExternoDestinatarioSelId() != null
                && flt.getOrgaoExternoDestinatarioSelId() != 0) {
            sbf.append(" and doc.orgaoExternoDestinatario.idOrgao = :orgaoExternoDestinatarioSelId");
        }

        if (flt.getCadastranteSelId() != null && flt.getCadastranteSelId() != 0) {
            sbf.append(" and doc.cadastrante.idPessoaIni = :cadastranteSelId");
        }

        if (flt.getLotaCadastranteSelId() != null
                && flt.getLotaCadastranteSelId() != 0) {
            sbf.append(" and doc.lotaCadastrante.idLotacaoIni = :lotaCadastranteSelId");
        }

        if (flt.getSubscritorSelId() != null && flt.getSubscritorSelId() != 0) {
            sbf.append(" and doc.subscritor.idPessoaIni = :subscritorSelId");
        }

        if (flt.getRequerenteDocSelId() != null && flt.getRequerenteDocSelId() != 0) {
            sbf.append(" and doc.requerenteDoc.idRequerente = :requerenteDocSelId");
        }

        if (flt.getNmSubscritorExt() != null
                && !flt.getNmSubscritorExt().trim().isEmpty()) {
            sbf.append(" and upper(doc.nmSubscritorExt) like :nmSubscritorExt");
        }

        if (flt.getOrgaoExternoSelId() != null
                && flt.getOrgaoExternoSelId() != 0) {
            sbf.append(" and doc.orgaoExterno.idOrgao = :orgaoExternoSelId");
        }

        if (flt.getNumExtDoc() != null && !flt.getNumExtDoc().trim().isEmpty()) {
            sbf.append(" and upper(doc.numExtDoc) like :numExtDoc");
        }

        if (flt.getIdMod() != null && flt.getIdMod() != 0) {
            sbf.append(" and exMod.hisIdIni = :hisIdIni");
        }

        if (flt.getListaIdDoc() != null && !flt.getListaIdDoc().isEmpty()) {
            sbf.append(" and (");

            for (int i = 0; i <= flt.getListaIdDoc().size() / 1000; i++)
                sbf.append(" doc.idDoc IN :listaIdDoc").append(i).append(" or");

            sbf.delete(sbf.length() - 3, sbf.length()).append(")");
        }

        if (!apenasCount) {
            if (flt.getOrdem() == null || flt.getOrdem() == 0)
                sbf.append(" order by doc.dtDoc desc, doc.idDoc desc");
            if (flt.getOrdem() == 1)
                sbf.append(" order by doc.dtDoc, doc.idDoc");
            else if (flt.getOrdem() == 2)
                sbf.append(" order by label.dtIniMarca desc, doc.idDoc desc");
            else if (flt.getOrdem() == 3)
                sbf.append(" order by label.dtIniMarca, doc.idDoc");
            else if (flt.getOrdem() == 4)
                sbf.append(" order by doc.anoEmissao desc, doc.numExpediente desc, mob.numSequencia, doc.idDoc desc");
            else if (flt.getOrdem() == 5)
                sbf.append(" order by doc.dtFinalizacao desc, doc.idDoc desc");
            else if (flt.getOrdem() == 6)
                sbf.append(" order by doc.idDoc desc");
        }

        String s = sbf.toString();
        s = s.replace("where and", "where");

        return s;

    }

    public void setMontadorPrincipal(IMontadorQuery montadorQueryPrincipal) {
        // Este médodo não faz nada. É utilizado apenas para a extensão da busca
        // textual do SIGA.
    }
}