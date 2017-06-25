package com.holictechnology.kidfriendly.ejbs;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.holictechnology.kidfriendly.domain.dtos.CategoryDto;
import com.holictechnology.kidfriendly.domain.dtos.CompanyDto;
import com.holictechnology.kidfriendly.domain.dtos.HourDateDto;
import com.holictechnology.kidfriendly.domain.dtos.ImageDto;
import com.holictechnology.kidfriendly.domain.dtos.filters.CompanyFilterDto;
import com.holictechnology.kidfriendly.domain.dtos.result.ResultDto;
import com.holictechnology.kidfriendly.domain.entitys.Address;
import com.holictechnology.kidfriendly.domain.entitys.Category;
import com.holictechnology.kidfriendly.domain.entitys.CategoryCharacteristic;
import com.holictechnology.kidfriendly.domain.entitys.Characteristic;
import com.holictechnology.kidfriendly.domain.entitys.City;
import com.holictechnology.kidfriendly.domain.entitys.Company;
import com.holictechnology.kidfriendly.domain.entitys.CompanyCategoryCharacteristic;
import com.holictechnology.kidfriendly.domain.entitys.CompanyFoodType;
import com.holictechnology.kidfriendly.domain.entitys.CompanyWeekSchedule;
import com.holictechnology.kidfriendly.domain.entitys.FoodType;
import com.holictechnology.kidfriendly.domain.entitys.Image;
import com.holictechnology.kidfriendly.domain.entitys.Phone;
import com.holictechnology.kidfriendly.domain.entitys.Schedule;
import com.holictechnology.kidfriendly.domain.entitys.Week;
import com.holictechnology.kidfriendly.domain.entitys.pk.CategoryCharacteristicPK;
import com.holictechnology.kidfriendly.domain.entitys.pk.CompanyCategoryCharacteristicPK;
import com.holictechnology.kidfriendly.domain.entitys.pk.CompanyFoodTypePK;
import com.holictechnology.kidfriendly.domain.entitys.pk.CompanyWeekSchedulePK;
import com.holictechnology.kidfriendly.domain.enums.StatusKidFriendlyEnum;
import com.holictechnology.kidfriendly.ejbs.interfaces.CompanyLocal;
import com.holictechnology.kidfriendly.library.exceptions.KidFriendlyException;
import com.holictechnology.kidfriendly.library.utilites.ObjectUtilities;
import com.holictechnology.kidfriendly.mount.dto.CompanyToCompanyDto;


@Stateless
public class CompanyEJB extends AbstractEJB implements CompanyLocal {

    private static final long serialVersionUID = 1389485495399887684L;
    private static List<Image> images = new ArrayList<Image>();
    private static int KM_DISTANCE = 5;
    private static int KM_DEGREE = 111;
    private static Company companyAux = new Company();

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.holictechnology.kidfriendly.ejbs.interfaces.CompanyLocal#find(java.
     * lang.Long)
     */
    @Override
    @Transactional(value = TxType.SUPPORTS)
    public Company find(final Long primaryKey, final String ... lazyAttributes) throws KidFriendlyException {
        return find(Company.class, primaryKey, lazyAttributes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.holictechnology.kidfriendly.ejbs.interfaces.CompanyLocal#
     * listSuggestions(java.lang.Integer)
     */
    @Override
    @SuppressWarnings("unchecked")
    @Transactional(value = TxType.NOT_SUPPORTED)
    public Collection<CompanyDto> listSuggestions(final Integer limit) throws KidFriendlyException {
        illegalArgument(limit);
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT TEMP.* FROM (");
        sql.append("SELECT company.ID_COMPANY, company.MG_HOME, company.ST_HIGHLIGHT, company.NUM_RATE, company.DES_NAME ");
        sql.append("FROM COMPANY AS company ");
        sql.append("WHERE company.ST_ACTIVE = 1 AND company.MG_HOME IS NOT NULL ");
        sql.append("ORDER BY RAND() LIMIT :limit ");
        sql.append(") AS TEMP ORDER BY TEMP.ST_HIGHLIGHT DESC, TEMP.NUM_RATE DESC, TEMP.DES_NAME ");
        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("limit", limit);
        CompanyDto companyDto = null;
        Collection<CompanyDto> listCompanyDto = new LinkedList<CompanyDto>();

        for (Object [] item : (List<Object []>) query.getResultList()) {
            companyDto = new CompanyDto();
            companyDto.setIdCompany((Long) item[0]);
            companyDto.setMgHome((byte []) item[1]);
            listCompanyDto.add(companyDto);
        }

        return listCompanyDto;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.holictechnology.kidfriendly.ejbs.interfaces.CompanyLocal#listNextToMe
     * (java.lang.Integer, java.lang.Double, java.lang.Double)
     */
    @Override
    @SuppressWarnings("unchecked")
    @Transactional(value = TxType.NOT_SUPPORTED)
    public Collection<CompanyDto> listNextToMe(final Integer limit, final Double longitude, final Double latitude) throws KidFriendlyException {
        illegalArgument(limit);
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT company.ID_COMPANY, company.IMG_LOGO ");
        sql.append("FROM COMPANY AS company INNER JOIN ADDRESS AS address ON (address.ID_ADDRESS = company.ID_ADDRESS) ");
        sql.append("WHERE company.ST_ACTIVE = 1 AND company.IMG_LOGO IS NOT NULL ");
        sql.append("AND ST_CONTAINS(ST_ENVELOPE(LineString(POINT(:longitude-" + KM_DISTANCE + "/ABS(COS(RADIANS(:latitude))*" + KM_DEGREE + "), :latitude-("
                + KM_DISTANCE + "/" + KM_DEGREE + ")), POINT(:longitude+" + KM_DISTANCE + "/ABS(COS(RADIANS(:latitude))*" + KM_DEGREE + "), :latitude+("
                + KM_DISTANCE + "/" + KM_DEGREE + ")))), POINT(address.NUM_LONGITUDE, address.NUM_LATITUDE)) ");
        sql.append("ORDER BY company.ST_HIGHLIGHT DESC, company.NUM_RATE DESC, company.DES_NAME ");
        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("longitude", longitude);
        query.setParameter("latitude", latitude);
        query.setFirstResult(BigInteger.ZERO.intValue());
        query.setMaxResults(limit);
        CompanyDto companyDto = null;
        Collection<CompanyDto> listCompanyDto = new LinkedList<CompanyDto>();

        for (Object [] item : (List<Object []>) query.getResultList()) {
            companyDto = new CompanyDto();
            companyDto.setIdCompany((Long) item[0]);
            companyDto.setMgHome((byte []) item[1]);
            listCompanyDto.add(companyDto);
        }

        return listCompanyDto;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.holictechnology.kidfriendly.ejbs.interfaces.CompanyLocal#search(com.
     * holictechnology.kidfriendly.domain.dtos.filters.CompanyFilterDto)
     */
    @Override
    @Transactional(value = TxType.NOT_SUPPORTED)
    public ResultDto<CompanyDto> search(final CompanyFilterDto companyFilterDto) throws KidFriendlyException {
        illegalArgument(companyFilterDto);
        Query query = entityManager.createNativeQuery(createSqlCount(createSqlSearch(companyFilterDto, Boolean.FALSE)).toString());
        setParametersSqlSearch(query, companyFilterDto);
        companyFilterDto.getPaginatorDto().setSize(((Number) query.getSingleResult()).longValue());
        List<CompanyDto> companys = new LinkedList<CompanyDto>();

        if (companyFilterDto.getPaginatorDto().getSize() > BigInteger.ZERO.intValue()) {
            query = entityManager.createNativeQuery(createSqlSearch(companyFilterDto, Boolean.TRUE).toString());
            setParametersSqlSearch(query, companyFilterDto);
            setParametersPaginator(query, companyFilterDto.getPaginatorDto());
            // companys.addAll(createResult(query.getResultList()));
        }

        ResultDto<CompanyDto> resultDto = new ResultDto<CompanyDto>();
        resultDto.setPaginatorDto(companyFilterDto.getPaginatorDto());
        resultDto.setResults(companys);

        return resultDto;
    }

    /**
     * @param companyFilterDto
     * @return
     */
    private StringBuffer createSqlSearch(final CompanyFilterDto companyFilterDto, final boolean isOrderBy) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT company.ID_COMPANY, company.DES_NAME, company.IMG_LOGO, company.NUM_RATE, company.ST_HIGHLIGHT, city.DES_CITY, state.DES_SIGLA ");
        sql.append("FROM COMPANY AS company ");
        sql.append(createSqlSearch(companyFilterDto.getIdCategory(), companyFilterDto.getCharacteristics()));
        sql.append("INNER JOIN ADDRESS AS address ON (address.ID_ADDRESS = company.ID_ADDRESS) ");
        sql.append("INNER JOIN CITY AS city ON (city.ID_CITY = address.ID_CITY" + ((companyFilterDto.getIdCity() != null) ? " AND city.ID_CITY = :idCity" : "")
                + ") ");
        sql.append("INNER JOIN STATE AS state ON (state.ID_STATE = city.ID_STATE"
                + ((companyFilterDto.getIdState() != null) ? " AND state.ID_STATE = :idState" : "") + ") ");
        sql.append("WHERE company.ST_ACTIVE = 1 ");
        sql.append((ObjectUtilities.isNotEmptyOrNull(companyFilterDto.getDesNameCompany()) ? " AND company.DES_NAME LIKE :desNameCompany" : " "));
        sql.append(((companyFilterDto.isSuperKidFriendly()) ? "AND company.NUM_RATE = :superKidFriendly " : " "));
        sql.append(((companyFilterDto.getLongitude() != null && companyFilterDto.getLatitude() != null)
                ? "AND ST_CONTAINS(ST_ENVELOPE(LineString(POINT(:longitude-" + KM_DISTANCE + "/ABS(COS(RADIANS(:latitude))*" + KM_DEGREE + "), :latitude-("
                        + KM_DISTANCE + "/" + KM_DEGREE + ")), POINT(:longitude+" + KM_DISTANCE + "/ABS(COS(RADIANS(:latitude))*" + KM_DEGREE + "), :latitude+("
                        + KM_DISTANCE + "/" + KM_DEGREE + ")))), POINT(address.NUM_LONGITUDE, address.NUM_LATITUDE)) "
                : ""));
        sql.append("GROUP BY company.ID_COMPANY, company.DES_NAME, company.IMG_LOGO, company.NUM_RATE, company.ST_HIGHLIGHT, city.DES_CITY, state.DES_SIGLA ");
        sql.append(((isOrderBy) ? "ORDER BY company.ST_HIGHLIGHT DESC, company.NUM_RATE DESC, company.DES_NAME " : " "));

        return sql;
    }

    /**
     * @param idsCharacteristic
     * @return
     */
    private StringBuffer createSqlSearch(final Integer idCategory, final List<Long> characteristics) {
        StringBuffer sql = new StringBuffer();

        if (idCategory != null && ObjectUtilities.isNotEmptyOrNull(characteristics)) {
            int size = characteristics.size();

            for (int index = 0; index < size; index++) {
                sql.append("INNER JOIN COMPANY_CATEGORY_CHARACTERISTIC AS companyCategoryCharacteristic" + index
                        + " ON (company.ID_COMPANY = companyCategoryCharacteristic" + index + ".ID_COMPANY and companyCategoryCharacteristic" + index
                        + ".ID_CATEGORY = :idCategory and companyCategoryCharacteristic" + index + ".ID_CHARACTERISTIC = :characteristc" + index + ") ");
            }
        } else if (idCategory != null) {
            sql.append(
                    "INNER JOIN COMPANY_CATEGORY_CHARACTERISTIC AS companyCategoryCharacteristic ON (company.ID_COMPANY = companyCategoryCharacteristic.ID_COMPANY and companyCategoryCharacteristic.ID_CATEGORY = :idCategory) ");
        }

        return sql;
    }

    /**
     * @param query
     * @param companyFilterDto
     */
    private void setParametersSqlSearch(final Query query, final CompanyFilterDto companyFilterDto) {
        if (ObjectUtilities.isNotEmptyOrNull(companyFilterDto.getDesNameCompany())) {
            query.setParameter("desNameCompany", "%" + companyFilterDto.getDesNameCompany() + "%");
        }

        if (companyFilterDto.getIdCity() != null) {
            query.setParameter("idCity", companyFilterDto.getIdCity());
        }

        if (companyFilterDto.getIdState() != null) {
            query.setParameter("idState", companyFilterDto.getIdState());
        }

        if (companyFilterDto.isSuperKidFriendly()) {
            query.setParameter("superKidFriendly", StatusKidFriendlyEnum.SUPER.getValue());
        }

        setParametersSqlSearch(query, companyFilterDto.getIdCategory(), companyFilterDto.getCharacteristics());

        if (companyFilterDto.getLongitude() != null && companyFilterDto.getLatitude() != null) {
            query.setParameter("longitude", companyFilterDto.getLongitude());
            query.setParameter("latitude", companyFilterDto.getLatitude());
        }
    }

    /**
     * @param query
     * @param characteristics
     */
    private void setParametersSqlSearch(final Query query, final Integer idCategory, final List<Long> characteristics) {
        if (idCategory != null) {
            query.setParameter("idCategory", idCategory);
        }

        if (ObjectUtilities.isNotEmptyOrNull(characteristics)) {
            int index = 0;

            for (Long characteristic : characteristics) {
                query.setParameter("characteristc" + index++, characteristic);
            }
        }
    }

    @Override
    public CompanyDto saveCompany(CompanyDto companyDto) throws KidFriendlyException {
        Company company = CompanyToCompanyDto.getInstance().companyDtoToCompany(companyDto);
        City city = entityManager.find(City.class, Integer.valueOf(companyDto.getAddressDto().getCityDto().getIdCity()));
        Address address = CompanyToCompanyDto.getInstance().mountAddress(companyDto, city);

        persist(address);
        company.setAddress(address);
        company.setDtRegister(new Date());
        company.setStActive(Boolean.TRUE);
        company.setStHighlight(Boolean.FALSE);
        company.setImgLogo(companyAux.getImgLogo());
        company.setMgHome(companyAux.getMgHome());

        company = entityManager.merge(company);
        companyAux = new Company();

        companyDto.setIdCompany(company.getIdCompany());
        
        saveHourDate(company, companyDto.getHourDateDtos());
        savePhone(companyDto, company);
        saveTypeFood(companyDto.getTypeFood(), company);

        images = new ArrayList<Image>();

        saveImage(company);

        saveCategoryCharacteristics(company, companyDto);

        return companyDto;
    }
    
    private void saveTypeFood(Long type, Company company){
    	CompanyFoodTypePK companyFoodTypePK = new CompanyFoodTypePK();
    	companyFoodTypePK.setCompany(company);
    	FoodType foodType = new FoodType();
    	foodType.setIdFoodType(type);
    	companyFoodTypePK.setFoodType(foodType);
    	CompanyFoodType companyFoodType = new CompanyFoodType();
    	companyFoodType.setCompanyFoodTypePK(companyFoodTypePK);
    	
    	entityManager.persist(companyFoodType);
    }
    
    /**
     * Save hour and date
     * @param company
     * @param hourDateDtos
     */
    private void saveHourDate(Company company, List<HourDateDto> hourDateDtos){
    	for(HourDateDto dto : hourDateDtos){
    		CompanyWeekSchedulePK companyWeekSchedulePK = new CompanyWeekSchedulePK();
    		Week week = new Week();
    		Schedule schedule = new Schedule();
    		Schedule scheduleFinish = new Schedule();
    		week.setIdWeek(dto.getWeek());
    		schedule.setIdSchedule(dto.getHourInitial());
    		scheduleFinish.setIdSchedule(dto.getHourFinish());
    		
    		companyWeekSchedulePK.setCompany(company);
    		companyWeekSchedulePK.setSchedule(schedule);
    		companyWeekSchedulePK.setScheduleFinish(scheduleFinish);
    		companyWeekSchedulePK.setWeek(week);
    		
    		CompanyWeekSchedule companyWeekSchedule = new CompanyWeekSchedule();
    		companyWeekSchedule.setCompanyWeekSchedulePK(companyWeekSchedulePK);
    		
    		entityManager.persist(companyWeekSchedule);
    	}
    }

    /**
     * Method in save data category and carachteristic
     * 
     * @param company
     * @param companyDto
     * @throws KidFriendlyException
     */
    private void saveCategoryCharacteristics(Company company, CompanyDto companyDto) throws KidFriendlyException {
        if (companyDto.getCategoryDtos() != null) {
            if (!companyDto.getCategoryDtos().isEmpty()) {
                for (CategoryDto categoryDto : companyDto.getCategoryDtos()) {
                    Category category = entityManager.find(Category.class, categoryDto.getCategory());
                    Characteristic character = entityManager.find(Characteristic.class, categoryDto.getCharacteristcs());
                    CategoryCharacteristic categoryCharacteristic = new CategoryCharacteristic();
                    CategoryCharacteristicPK categoryCharacteristicPK = new CategoryCharacteristicPK();
                    categoryCharacteristicPK.setCategory(category);
                    categoryCharacteristicPK.setCharacteristic(character);
                    categoryCharacteristic.setCategoryCharacteristicPK(categoryCharacteristicPK);
                    CompanyCategoryCharacteristicPK companyCategoryCharacteristicPK = new CompanyCategoryCharacteristicPK();
                    companyCategoryCharacteristicPK.setCategoryCharacteristic(categoryCharacteristic);
                    companyCategoryCharacteristicPK.setCompany(company);
                    CompanyCategoryCharacteristic companyCategoryCharacteristic = new CompanyCategoryCharacteristic();
                    companyCategoryCharacteristic.setCompanyCategoryCharacteristicPK(companyCategoryCharacteristicPK);
                    
                    CompanyCategoryCharacteristic categoryCharacterist = new CompanyCategoryCharacteristic();
                    categoryCharacterist.setCompanyCategoryCharacteristicPK(companyCategoryCharacteristicPK);
                    
                    persist(categoryCharacterist);
                }
            }
        }
    }

    /**
     * Method save photos reference company
     * 
     * @param company
     * @throws KidFriendlyException
     */
    private void saveImage(Company company) throws KidFriendlyException {
        if (!images.isEmpty()) {
            for (Image image : images) {
                image.setCompany(company);
                persist(image);
            }
        }
    }

    /**
     * Method save phones by company
     * 
     * @param companyDto
     * @param company
     * @throws KidFriendlyException
     */
    private void savePhone(CompanyDto companyDto, Company company) throws KidFriendlyException {
        List<Phone> phones = CompanyToCompanyDto.getInstance().mountPhone(companyDto, company);
        for (Phone phone : phones) {
            persist(phone);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(value = TxType.NOT_SUPPORTED)
    public List<Company> searchCompanySimple(String nameEstablishment, String responsibleEstablishment, String cnpj,
            Integer objCity) {
        StringBuffer sql = new StringBuffer();

        sql.append(
                " SELECT DISTINCT c FROM Company c JOIN FETCH c.phones p JOIN FETCH c.address a JOIN FETCH a.city ci JOIN FETCH ci.state s WHERE c.address.city.idCity = :idCity ");
        sql.append(" AND c.stActive = :stActive ");

        if (nameEstablishment != null && !nameEstablishment.equals("undefined")) {
            sql.append(" AND c.desName LIKE :desName ");
        }

        if (responsibleEstablishment != null && !responsibleEstablishment.equals("undefined")) {
            sql.append(" AND c.desNameResponsible LIKE :desNameResponsible ");
        }

        if (cnpj != null && !cnpj.equals("undefined")) {
            sql.append(" AND c.desCNPJ = :desCNPJ ");
        }

        Query query = entityManager.createQuery(sql.toString());
        query.setParameter("idCity", objCity);
        query.setParameter("stActive", Boolean.TRUE);

        if (nameEstablishment != null && !nameEstablishment.equals("undefined"))
            query.setParameter("desName", "%" + nameEstablishment + "%");

        if (responsibleEstablishment != null && !responsibleEstablishment.equals("undefined"))
            query.setParameter("desNameResponsible", "%" + responsibleEstablishment + "%");

        if (cnpj != null && !cnpj.equals("undefined"))
            query.setParameter("desCNPJ", cnpj);

        List<Company> companies = query.getResultList();

        if (companies == null)
            return null;

        return companies;
    }

    @Override
    public Company inactivateCompany(Company company) {
        company.setStActive(Boolean.FALSE);

        try {
            return merge(company);
        } catch (KidFriendlyException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Company editCompany(Company company) {
        try {
            return merge(company);
        } catch (KidFriendlyException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void preparImageSaveCompany(ImageDto imageDto) {
        Image image = null;

        if(imageDto.getType() == 0){
	        if (!images.isEmpty()) {
	            image = images.get(0);
	        }
	
	        image = create(imageDto);
	        images.add(image);
        }else if(imageDto.getType() == 1){
        	companyAux.setImgLogo(imageDto.getImgImage());
        }else{
        	companyAux.setMgHome(imageDto.getImgImage());
        }
    }

    /**
     * @param imageDto
     * @return
     */
    private Image create(ImageDto imageDto) {
        Image image = new Image();
        image.setCompany(new Company());
        image.setDesImage(imageDto.getDesImage());
        image.setImgImage(imageDto.getImgImage());
        image.getCompany().setDesName(imageDto.getNameCompany());

        return image;
    }
}
