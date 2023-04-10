package br.gov.jfrj.siga.ex.util;

import java.util.Date;
import java.util.TimeZone;

import br.gov.jfrj.siga.base.Prop;
import junit.framework.TestCase;

public class DocumentoUtilTest extends TestCase {

    public void testUFNoFinal() {
        Prop.setProvider(new Prop.IPropertyProvider() {
            @Override
            public String getProp(String nome) {
                return null;
            }

            @Override
            public void addPrivateProperty(String name) {

            }

            @Override
            public void addRestrictedProperty(String name) {

            }

            @Override
            public void addPublicProperty(String name) {

            }

            @Override
            public void addPrivateProperty(String name, String defaultValue) {

            }

            @Override
            public void addRestrictedProperty(String name, String defaultValue) {

            }

            @Override
            public void addPublicProperty(String name, String defaultValue) {

            }
        });
        long lilis = 1510884000000L;


        int offset = TimeZone.getDefault().getOffset(lilis);
        Date dt17Nov2017 = new Date(lilis - offset);

        assertEquals("Campo dos Goytacazes-RJ, 17 de novembro de 2017.", DocumentoUtil.obterDataExtenso("CAMPO DOS GOYTACAZES-RJ", dt17Nov2017));
        assertEquals("Campo dos Goytacazes-RJ, 17 de novembro de 2017.", DocumentoUtil.obterDataExtenso("campo dos goyTACAZes-RJ", dt17Nov2017));

        assertEquals("Campo dos Goytacazes, 17 de novembro de 2017.", DocumentoUtil.obterDataExtenso("campo dos goyTACAZes", dt17Nov2017));
        assertEquals("Campo dos Goytacazes, 17 de novembro de 2017.", DocumentoUtil.obterDataExtenso("CAMPO DOS GOYTACAZES", dt17Nov2017));
        assertEquals("Campo dos Goytacazes, 17 de novembro de 2017.", DocumentoUtil.obterDataExtenso("campo dos goytacazes", dt17Nov2017));

        assertEquals("Qualquer Coisa-RJ, 17 de novembro de 2017.", DocumentoUtil.obterDataExtenso("qualquer coisa-rj", dt17Nov2017));
        assertEquals("Duque de Caxias-RJ, 17 de novembro de 2017.", DocumentoUtil.obterDataExtenso("DuQuE dE cAxIaS-rJ", dt17Nov2017));

        assertEquals("Ji-Paraná-RO, 17 de novembro de 2017.", DocumentoUtil.obterDataExtenso("jI-pArAnÁ-RO", dt17Nov2017));

    }
}
