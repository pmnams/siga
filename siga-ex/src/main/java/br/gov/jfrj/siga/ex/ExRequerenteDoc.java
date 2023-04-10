package br.gov.jfrj.siga.ex;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.swing.text.MaskFormatter;

import br.gov.jfrj.siga.model.Selecionavel;
import org.hibernate.annotations.BatchSize;
import org.hibernate.search.annotations.Indexed;

@Entity
@Indexed
@Cacheable
@BatchSize(size = 500)
@Table(name = "siga.ex_requerente")
public class ExRequerenteDoc extends AbstractExRequerenteDoc implements Selecionavel {

	private static final long serialVersionUID = -1981963049721664626L;
	
	public String getCpfFormatado() {
    	MaskFormatter mf;
		try {
			mf = new MaskFormatter("###.###.###-##");
	        mf.setValueContainsLiteralCharacters(false);
	        return mf.valueToString(this.getCpfRequerente());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return this.getCpfRequerente();
    }
	
	public String getDtNascimentoFormatado() {
		Date data = this.getDataNascimento();

		if (Objects.isNull(data))
			return "";

		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		return df.format(data);
	}

	@Override
	public String getSigla() {
		return getCpfRequerente();
	}

	@Override
	public void setSigla(String sigla) {

	}

	@Override
	public String getDescricao() {
		return this.getNomeRequerente();
	}
}
