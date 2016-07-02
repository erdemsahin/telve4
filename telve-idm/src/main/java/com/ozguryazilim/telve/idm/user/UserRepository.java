/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.telve.idm.user;

import com.google.common.base.Strings;
import com.ozguryazilim.telve.data.RepositoryBase;
import com.ozguryazilim.telve.idm.entities.User;
import com.ozguryazilim.telve.idm.entities.UserGroup;
import com.ozguryazilim.telve.idm.entities.UserGroup_;
import com.ozguryazilim.telve.idm.entities.User_;
import com.ozguryazilim.telve.query.QueryDefinition;
import com.ozguryazilim.telve.query.filters.Filter;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

/**
 * Kullanıcı verilerine erişim için repository sınıf
 * 
 * @author Hakan Uygun
 */
@Repository
@Dependent
public abstract class UserRepository extends RepositoryBase<User, UserViewModel> implements CriteriaSupport<User>{

    public User createNew() throws InstantiationException, IllegalAccessException {
        User result = super.createNew();
        result.setUserType("STANDART");
        return result;
    }

    @Override
    public List<UserViewModel> browseQuery( QueryDefinition queryDefinition ){
        List<Filter<User, ?>> filters = queryDefinition.getFilters();
        
        CriteriaBuilder criteriaBuilder = entityManager().getCriteriaBuilder();
        //Geriye PersonViewModel dönecek cq'yu ona göre oluşturuyoruz.
        CriteriaQuery<UserViewModel> criteriaQuery = criteriaBuilder.createQuery(UserViewModel.class);

        //From Tabii ki User
        Root<User> from = criteriaQuery.from(User.class);
        
        
        //Sonuç filtremiz
        criteriaQuery.multiselect(
                from.get(User_.id),
                from.get(User_.loginName),
                from.get(User_.firstName),
                from.get(User_.lastName),
                from.get(User_.email),
                from.get(User_.active),
                from.get(User_.userType),
                from.get(User_.info)
        );
        
        //Filtreleri ekleyelim.
        List<Predicate> predicates = new ArrayList<>();
        
        if( !queryDefinition.getExtraFilters().isEmpty() ){
            //Gelen extra filtre UserGroup bilgisi içermeli
            Filter<?,?> f = (Filter<?,?>) queryDefinition.getExtraFilters().get(0);
            if( f.getAttribute().equals(UserGroup_.group) && f.getValue() != null ){
                Subquery<UserGroup> subquery = criteriaQuery.subquery(UserGroup.class);
                Root fromUserGroup = subquery.from(UserGroup.class);
                //Join<UserGroup, User> sqUser = fromUserGroup.join(UserGroup_.user);
                subquery.select(fromUserGroup.get(User_.id));
            
                List<Predicate> subPredicates = new ArrayList<>();
                f.decorateCriteriaQuery(subPredicates, criteriaBuilder, fromUserGroup);
                subPredicates.add(criteriaBuilder.equal(fromUserGroup.get(UserGroup_.user), from));
                
                subquery.where(subPredicates.toArray(new Predicate[]{}));
                
                predicates.add(criteriaBuilder.exists(subquery));
            }
        }
        
        //FIXME: Burada Grup Manager olanlar için grup bazlı bir sorgu lazım
        //Root<UserOrganization> uoFrom = criteriaQuery.from(UserOrganization.class);
        //predicates.add( criteriaBuilder.equal(uoFrom.get(UserOrganization_.username), userLookup.getActiveUser().getLoginName()));
        //predicates.add( criteriaBuilder.equal(from.get(Capa_.organization), uoFrom.get(UserOrganization_.organization)));
        
        decorateFilters(filters, predicates, criteriaBuilder, from);

        if (!Strings.isNullOrEmpty(queryDefinition.getSearchText())) {
            predicates.add(criteriaBuilder.or(criteriaBuilder.like(from.get(User_.loginName), "%" + queryDefinition.getSearchText() + "%"),
                    criteriaBuilder.like(from.get(User_.firstName), "%" + queryDefinition.getSearchText() + "%"),
                    criteriaBuilder.like(from.get(User_.lastName), "%" + queryDefinition.getSearchText() + "%")));
        }
        
        //Person filtremize ekledik.
        criteriaQuery.where(predicates.toArray(new Predicate[]{}));
        
        // Öncelikle default sıralama verelim eğer kullanıcı tarafından tanımlı sıralama var ise onu setleyelim
        if ( queryDefinition.getSorters().isEmpty() )  {
            criteriaQuery.orderBy(criteriaBuilder.asc(from.get(User_.loginName)));
        } else {
            criteriaQuery.orderBy(decorateSorts(queryDefinition.getSorters(), criteriaBuilder, from));
        }
        
        //Haydi bakalım sonuçları alalım
        TypedQuery<UserViewModel> typedQuery = entityManager().createQuery(criteriaQuery);
        typedQuery.setMaxResults(queryDefinition.getResultLimit());
        List<UserViewModel> resultList = typedQuery.getResultList();

        return resultList;
    }

    @Override
    public List<User> suggestion(String searchText) {
        return criteria()
                .or(
                        criteria().like(User_.loginName, "%" + searchText + "%"),
                        criteria().like(User_.firstName, "%" + searchText + "%"),
                        criteria().like(User_.lastName, "%" + searchText + "%")
                )
                .eq(User_.active, true)
                .createQuery()
                .setMaxResults(100)
                .getResultList();
    }
    
    @Override
    public List<UserViewModel> lookupQuery(String searchText) {
        
        CriteriaBuilder criteriaBuilder = entityManager().getCriteriaBuilder();
        //Geriye PersonViewModel dönecek cq'yu ona göre oluşturuyoruz.
        CriteriaQuery<UserViewModel> criteriaQuery = criteriaBuilder.createQuery(UserViewModel.class);

        //From Tabii ki User
        Root<User> from = criteriaQuery.from(User.class);
        
        
        //Sonuç filtremiz
        criteriaQuery.multiselect(
                from.get(User_.id),
                from.get(User_.loginName),
                from.get(User_.firstName),
                from.get(User_.lastName),
                from.get(User_.email),
                from.get(User_.active),
                from.get(User_.userType),
                from.get(User_.info)
        );
        
        //Filtreleri ekleyelim.
        List<Predicate> predicates = new ArrayList<>();
        
        //FIXME: Burada Grup Manager olanlar için grup bazlı bir sorgu lazım
        //Root<UserOrganization> uoFrom = criteriaQuery.from(UserOrganization.class);
        //predicates.add( criteriaBuilder.equal(uoFrom.get(UserOrganization_.username), userLookup.getActiveUser().getLoginName()));
        //predicates.add( criteriaBuilder.equal(from.get(Capa_.organization), uoFrom.get(UserOrganization_.organization)));
        
        predicates.add(
                criteriaBuilder.or( 
                    criteriaBuilder.like(from.get( User_.loginName ), "%" + searchText + "%"),
                    criteriaBuilder.like(from.get( User_.firstName ), "%" + searchText + "%"),
                    criteriaBuilder.like(from.get( User_.lastName ), "%" + searchText + "%")));

        
        //Person filtremize ekledik.
        criteriaQuery.where(predicates.toArray(new Predicate[]{}));
        
        // Öncelikle default sıralama verelim eğer kullanıcı tarafından tanımlı sıralama var ise onu setleyelim
        criteriaQuery.orderBy(criteriaBuilder.asc(from.get(User_.loginName)));
        
        
        //Haydi bakalım sonuçları alalım
        TypedQuery<UserViewModel> typedQuery = entityManager().createQuery(criteriaQuery);
        List<UserViewModel> resultList = typedQuery.getResultList();

        return resultList;
    }
    
    public abstract User findAnyByLoginName( String loginname );
    
    public abstract List<User> findByUserTypeAndActive( String userType, Boolean active);
}
