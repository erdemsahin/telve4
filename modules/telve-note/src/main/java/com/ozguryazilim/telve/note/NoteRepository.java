package com.ozguryazilim.telve.note;

import com.ozguryazilim.telve.data.RepositoryBase;
import com.ozguryazilim.telve.entities.FeaturePointer;
import com.ozguryazilim.telve.entities.Note;
import com.ozguryazilim.telve.entities.Note_;
import java.util.List;
import javax.enterprise.context.Dependent;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.Criteria;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

/**
 * Note entitysi için repository sınıfı.
 * 
 * @author Hakan Uygun
 */
@Repository
@Dependent
public abstract class NoteRepository extends RepositoryBase<Note, Note> implements CriteriaSupport<Note>{
    
    public List<Note> findNotes( String username, String attachment ){
        Criteria<Note,Note> crit = criteria();
        
        //TODO: Buraya daha sonra kişiye özel not kontrolü de eklenecek.
        //crit.or(crit.eq(Note_.owner, username), crit.eq( Note_.permission, "ALL"));
        crit.eq(Note_.attachtment, attachment);
        
        return crit.getResultList();
    }
    
    
    /**
     * Geriye Feature ile ilgili activity'leri döndürür.
     * @param featurePointer
     * @return 
     */
    public List<Note> findByFeature( FeaturePointer featurePointer ){
        Criteria<Note,Note> crit = criteria()
                .eq(Note_.featurePointer, featurePointer)
                .orderAsc(Note_.createDate);
                
        /*
        switch( filter ){
            case MINE : crit.eq(Activity_.assignee, identity.getLoginName()); break;
            case OVERDUE : crit.gt(Activity_.dueDate, new Date()); break;
            case SCHEDULED : crit.eq( Activity_.status, ActivityStatus.SCHEDULED); break;
            case SUCCESS : crit.eq( Activity_.status, ActivityStatus.SUCCESS); break;
            case FAILED : crit.eq( Activity_.status, ActivityStatus.FAILED); break;
            case FOLLOWUP : break; //TODO:
        }
        */
        
        //Criteria üzerinde pagein limit yok
        //return crit.createQuery().setMaxResults(5).getResultList();
        return crit.getResultList();
    }
}
