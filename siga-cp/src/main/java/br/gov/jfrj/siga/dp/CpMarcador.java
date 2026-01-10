/*******************************************************************************
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
/*
 * Criado em  21/12/2005
 *
 */
package br.gov.jfrj.siga.dp;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;

import br.gov.jfrj.siga.cp.model.enm.CpMarcadorEnum;
import br.gov.jfrj.siga.cp.model.enm.CpMarcadorFinalidadeGrupoEnum;
import br.gov.jfrj.siga.cp.model.enm.CpMarcadorTipoAplicacaoEnum;
import br.gov.jfrj.siga.cp.model.enm.CpMarcadorTipoInteressadoEnum;
import br.gov.jfrj.siga.model.ActiveRecord;
import br.gov.jfrj.siga.model.Assemelhavel;
import br.gov.jfrj.siga.sinc.lib.SincronizavelSuporte;

@Entity
// @Cacheable(false)
// @Cache(region = CpDao.CACHE_HOURS, usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(name = "corporativo.cp_marcador")
public class CpMarcador extends AbstractCpMarcador {
	final static public int ID_MARCADOR_CANCELADO = 10;

	public static ActiveRecord<CpMarcador> AR = new ActiveRecord<>(CpMarcador.class);

	public static final List<Long> MARCADORES_DEMANDA_JUDICIAL = Arrays.asList(CpMarcadorEnum.DEMANDA_JUDICIAL_BAIXA.getId(),
			CpMarcadorEnum.DEMANDA_JUDICIAL_MEDIA.getId(), CpMarcadorEnum.DEMANDA_JUDICIAL_ALTA.getId());
	
	public static final List<Long> MARCADOR_A_DEVOLVER_FORA_DO_PRAZO = Collections.singletonList(CpMarcadorEnum.A_DEVOLVER_FORA_DO_PRAZO.getId());
	
	/**
	 * Ordena de acordo com a {@link #getOrdem() Ordem}.
	 */
	public static final Comparator<CpMarcador> ORDEM_COMPARATOR = Comparator
			.nullsFirst(Comparator.comparing(CpMarcador::getIdFinalidade,
					Comparator.nullsFirst(Comparator.naturalOrder())))
			.thenComparing(Comparator.comparing(CpMarcador::getOrdem, Comparator.nullsFirst(Comparator.naturalOrder())))
			.thenComparing(Comparator.comparing(CpMarcador::getDescrMarcador,
					Comparator.nullsFirst(Comparator.naturalOrder())));

	public static final Comparator<CpMarcador> GRUPO_COMPARATOR = Comparator
			.nullsFirst(Comparator.comparing(CpMarcador::getFinalidadeGrupo,
					Comparator.nullsFirst(Comparator.naturalOrder())))
			.thenComparing(Comparator.comparing(CpMarcador::getDescrMarcador,
					Comparator.nullsFirst(Comparator.naturalOrder())));

	public CpMarcador() {
		super();
	}

	public boolean isDemandaJudicial() {
		return MARCADORES_DEMANDA_JUDICIAL.contains(this.getIdMarcador());
	}
	
	public boolean isADevolverForaDoPrazo() {
		return MARCADOR_A_DEVOLVER_FORA_DO_PRAZO.contains(this.getIdMarcador());
	}

	@Override
	public Long getId() {
		return getIdMarcador();
	}

	@Override
	public void setId(Long id) {
		setIdMarcador(id);
		return;
	}

	public boolean semelhante(Assemelhavel obj, int nivel) {
		return SincronizavelSuporte.semelhante(this, obj, nivel);
	}

	public boolean isAplicacaoGeral() {
		return getIdFinalidade().getIdTpAplicacao() == CpMarcadorTipoAplicacaoEnum.GERAL;
	}

	public boolean isAplicacaoGeralOuViaEspecificaOuUltimoVolume() {
		return getIdFinalidade().getIdTpAplicacao() == CpMarcadorTipoAplicacaoEnum.VIA_ESPECIFICA_OU_ULTIMO_VOLUME;
	}

	public boolean isAplicacaoGeralOuTodasAsViasOuUltimoVolume() {
		return getIdFinalidade().getIdTpAplicacao() == CpMarcadorTipoAplicacaoEnum.TODAS_AS_VIAS_OU_ULTIMO_VOLUME;
	}

	public boolean isInteressadoAtentende() {
		return getIdFinalidade().getIdTpInteressado() == CpMarcadorTipoInteressadoEnum.ATENDENTE;
	}

	public boolean isInteressadoPessoa() {
		return getIdFinalidade().getIdTpInteressado() == CpMarcadorTipoInteressadoEnum.PESSOA
				|| getIdFinalidade().getIdTpInteressado() == CpMarcadorTipoInteressadoEnum.LOTACAO_OU_PESSOA
				|| getIdFinalidade().getIdTpInteressado() == CpMarcadorTipoInteressadoEnum.PESSOA_OU_LOTACAO;
	}

	public boolean isInteressadoLotacao() {
		return getIdFinalidade().getIdTpInteressado() == CpMarcadorTipoInteressadoEnum.LOTACAO
				|| getIdFinalidade().getIdTpInteressado() == CpMarcadorTipoInteressadoEnum.LOTACAO_OU_PESSOA
				|| getIdFinalidade().getIdTpInteressado() == CpMarcadorTipoInteressadoEnum.PESSOA_OU_LOTACAO;
	}
	
	public CpMarcadorFinalidadeGrupoEnum getFinalidadeGrupo() {
		return getIdFinalidade().getGrupo();
	}

}