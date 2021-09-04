package br.gov.jfrj.siga.ex;

import java.util.Date;

import javax.persistence.*;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import br.gov.jfrj.siga.model.Objeto;
import br.gov.jfrj.siga.sinc.lib.Desconsiderar;

@MappedSuperclass
@Indexed
public abstract class AbstractExRequerenteDoc extends Objeto{

	private static final long serialVersionUID = -3826185534003365842L;

	@Id
	@SequenceGenerator(name = "EX_REQUERENTE_SEQ", sequenceName = "SIGA.EX_REQUERENTE_SEQ")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "EX_REQUERENTE_SEQ")
	@Column(name = "ID_REQUERENTE", unique = true, nullable = false)
	@Desconsiderar
	private Long idRequerente;
	
	@Column(name = "CPF_REQUERENTE", nullable = false)
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)
	private String cpfRequerente;
	
	@Column(name = "NOME_REQUERENTE", nullable = false)
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)
	private String nomeRequerente;
	
	@Column(name = "ENDERECO_REQUERENTE")
	private String endereco;
	
	@Column(name = "NMENDERECO_REQUERENTE")
	private Integer numeroEndereco;

	@Column(name = "RG_REQUERENTE")
	private String rgRequerente;
	
	@Column(name = "TIPO_REQUERENTE")
	private Integer tipoRequerente;
	
	@Column(name = "BAIRRO_REQUERENTE")
	private String bairro;
	
	@Column(name = "CIDADE_REQUERENTE")
	private String cidade;
	
	@Column(name = "DATA_NASC_REQUERENTE")
	private Date dataNascimento;
	
	@Column(name = "ESTADO_CIVIL_REQUERENTE")
	private Integer estadoCivil;
	
	@Column(name = "NOME_CONJUGUE")
	private String nomeConjugue;
	
	@Column(name = "CONTATO_REQUERENTE")
	private String telefoneRequerente;
	
	@Column(name = "EMAIL_REQUERENTE")
	private String emailRequerente;
	
	@Column(name = "NACIONALIDADE_REQUERENTE")
	private String nacionalidadeRequerente;

	public Long getId() {
		return idRequerente;
	}

	public void setId(Long idRequerente) {
		this.idRequerente = idRequerente;
	}

	public Integer getNumeroEndereco() {
		return numeroEndereco;
	}

	public void setNumeroEndereco(Integer numeroEndereco) {
		this.numeroEndereco = numeroEndereco;
	}

	public String getNomeRequerente() {
		return nomeRequerente;
	}

	public void setNomeRequerente(String nomeRecquerente) {
		this.nomeRequerente = nomeRecquerente;
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public String getCpfRequerente() {
		return cpfRequerente;
	}

	public void setCpfRequerente(String cpfRequerente) {
		this.cpfRequerente = cpfRequerente;
	}

	public String getRgRequerente() {
		return rgRequerente;
	}

	public void setRgRequerente(String rgRequerente) {
		this.rgRequerente = rgRequerente;
	}

	public String getBairro() {
		return bairro;
	}

	public void setBairro(String bairro) {
		this.bairro = bairro;
	}

	public String getCidade() {
		return cidade;
	}

	public void setCidade(String cidade) {
		this.cidade = cidade;
	}

	public Date getDataNascimento() {
		return dataNascimento;
	}
	
	public Integer getTipoRequerente() {
		return tipoRequerente;
	}

	public void setTipoRequerente(Integer tipoRequerente) {
		this.tipoRequerente = tipoRequerente;
	}
	
	public void setDataNascimento(Date dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	public Integer getEstadoCivil() {
		return estadoCivil;
	}

	public void setEstadoCivil(Integer estadoCivil) {
		this.estadoCivil = estadoCivil;
	}

	public String getNomeConjugue() {
		return nomeConjugue;
	}

	public void setNomeConjugue(String nomeConjugue) {
		this.nomeConjugue = nomeConjugue;
	}

	public String getTelefoneRequerente() {
		return telefoneRequerente;
	}

	public void setTelefoneRequerente(String telefoneRequerente) {
		this.telefoneRequerente = telefoneRequerente;
	}

	public String getEmailRequerente() {
		return emailRequerente;
	}

	public void setEmailRequerente(String emailRequerente) {
		this.emailRequerente = emailRequerente;
	}

	public String getNacionalidadeRequerente() {
		return nacionalidadeRequerente;
	}

	public void setNacionalidadeRequerente(String nacionalidadeRequerente) {
		this.nacionalidadeRequerente = nacionalidadeRequerente;
	}
}
