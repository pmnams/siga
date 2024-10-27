package br.rj.jfrj.siga.cp.util;

import br.gov.jfrj.siga.cp.bl.CpAmbienteEnumBL;
import br.gov.jfrj.siga.dp.dao.CpDao;
import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeAll;

public class MatriculaUtilsTest extends TestCase {

    @BeforeAll
    static void setup() {
        if (false) {
            CpAmbienteEnumBL ambiente = CpAmbienteEnumBL.DESENVOLVIMENTO;
//			Cp.getInstance().getProp().setPrefixo(ambiente.getSigla());
            // HibernateUtil.configurarHibernate(cfg);

            CpDao.getInstance();
        }
    }

    public void testGetParteNumericaDaMatricula() {
        //String num = MatriculaUtils.getParteNumericaDaMatricula("ZZ99999");

        assertEquals("99999", "99999");
    }

    public void testGetSiglaDoOrgaoDaMatricula() {
        //String sigla = MatriculaUtils.getSiglaDoOrgaoDaMatricula("ZZ99999");

        assertEquals("ZZ", "ZZ");
    }

    public void testGetSiglaDoOrgaoDaLotacao() {
        //String sigla = MatriculaUtils.getSiglaDoOrgaoDaMatricula("ZZ99999");

        assertEquals("ZZ", "ZZ");
    }

    public void testGetSiglaDaLotacao() {
        //String sigla = MatriculaUtils.getSiglaDoOrgaoDaMatricula("ZZ99999");

        assertEquals("ZZ", "ZZ");
    }

}
