package br.gov.jfrj.siga.persistencia;

import java.util.List;

import br.gov.jfrj.siga.ex.ExRequerenteDoc;

import javax.persistence.EntityManager;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class ExRequerenteSearch {
	private final EntityManager em;
	private Long totalResults;

	public ExRequerenteSearch(EntityManager em) {
		this.em = em;
	}

	public Long getTotalResultados() {
		return this.totalResults;
	}

	public List<ExRequerenteDoc> search(Integer page, Integer maxResults, String ref) {
		CriteriaBuilder cb = em.getCriteriaBuilder();

		CriteriaQuery<ExRequerenteDoc> q = cb.createQuery(ExRequerenteDoc.class);
		Root<ExRequerenteDoc> r = q.from(ExRequerenteDoc.class);
		q.select(r);

		if(ref != null && !ref.isEmpty())
			q.where(cb.or(
				cb.like(r.get("nomeRequerente").as(String.class), "%" + ref + "%"),
				cb.like(r.get("cpfRequerente").as(String.class), "%" + ref + "%")
			));
		q.orderBy(cb.asc(r.get("nomeRequerente")));

		TypedQuery<ExRequerenteDoc> query = em.createQuery(q);

		query.setFirstResult(maxResults * page);
		query.setMaxResults(maxResults);

		this.totalResults = count(ref);

		return query.getResultList();
	}

	private Long count(String ref) {
		CriteriaBuilder cb = em.getCriteriaBuilder();

		CriteriaQuery<Long> q = cb.createQuery(Long.class);
		Root<ExRequerenteDoc> r = q.from(ExRequerenteDoc.class);
		q.select(cb.count(r));

		if(ref != null && !ref.isEmpty())
			q.where(cb.or(
					cb.like(r.get("nomeRequerente").as(String.class), "%" + ref + "%"),
					cb.like(r.get("cpfRequerente").as(String.class), "%" + ref + "%")
			));
		q.orderBy(cb.asc(r.get("nomeRequerente")));

		TypedQuery<Long> query = em.createQuery(q);

		return query.getSingleResult();
	}

	public ExRequerenteDoc getByCpf(String cpf) {
		CriteriaBuilder cb = em.getCriteriaBuilder();

		CriteriaQuery<ExRequerenteDoc> q = cb.createQuery(ExRequerenteDoc.class);
		Root<ExRequerenteDoc> r = q.from(ExRequerenteDoc.class);
		q.select(r);

		q.where(cb.equal(r.get("cpfRequerente").as(String.class), cpf));

		TypedQuery<ExRequerenteDoc> query = em.createQuery(q);

		try {
			return query.getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}

}
