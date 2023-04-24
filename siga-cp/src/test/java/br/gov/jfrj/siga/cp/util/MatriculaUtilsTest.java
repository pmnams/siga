package br.gov.jfrj.siga.cp.util;

import org.junit.Test;

import static org.junit.Assert.*;
import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.cp.util.MatriculaUtils;

public class MatriculaUtilsTest {
	
	@Test( expected = AplicacaoException.class )
	public void deveLancarExcecaoQuandoAMatriculaForNula() throws Exception {
		MatriculaUtils.validaPreenchimentoMatricula( null );
	}
	
	@Test( expected = AplicacaoException.class )
	public void deveLancarExcecaoQuandoAMatriculaEstiverVazia() throws Exception {
		MatriculaUtils.validaPreenchimentoMatricula( "" );
	}
	
	@Test( expected = AplicacaoException.class )
	public void deveLancarExcecaoCasoOTamanhoDaMatriculaSejaMenorOuIgualADois() throws Exception {
		MatriculaUtils.validaPreenchimentoMatricula( "RJ" );
	}

}
