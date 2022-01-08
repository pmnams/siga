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
package br.gov.jfrj.siga.base;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class VO {
    List<AcaoVO> acoes = new ArrayList<>();

    private static class NomeAcaoVOComparator implements Comparator<AcaoVO> {

        public int compare(AcaoVO o1, AcaoVO o2) {
            int i = Boolean.compare(o1.isPode(), o2.isPode());
            if (i != 0)
                return -i;
            return o1.getNome().replace("_", "").compareTo(o2.getNome().replace("_", ""));
        }
    }

    public List<AcaoVO> getAcoes() {
        return acoes;
    }

    public List<AcaoVO> getAcoesOrdenadasPorNome() {
        return AcaoVO.ordena(getAcoes(), new NomeAcaoVOComparator());
    }

    public void setAcoes(List<AcaoVO> acoes) {
        this.acoes = acoes;
    }

    public void addAcao(AcaoVO acao) {
        acoes.add(acao);
    }
}
