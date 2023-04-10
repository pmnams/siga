package br.gov.jfrj.siga.ex.converter;

import br.gov.jfrj.siga.cp.converter.EnumWithIdConverter;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeVinculo;

import javax.persistence.Converter;

@Converter(autoApply = true)
public class ExTipoDeVinculoConverter extends EnumWithIdConverter<ExTipoDeVinculo> {

}
