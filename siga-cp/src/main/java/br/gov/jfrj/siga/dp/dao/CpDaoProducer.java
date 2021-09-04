package br.gov.jfrj.siga.dp.dao;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

@RequestScoped
public class CpDaoProducer {
	@Produces
	@RequestScoped
	public CpDao getInstance() {
		return CpDao.getInstance();
	}

	public void close(@Disposes CpDao cpDao) {
		CpDao.freeInstance();
	}

}
