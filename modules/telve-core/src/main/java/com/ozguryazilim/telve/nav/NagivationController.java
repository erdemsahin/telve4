package com.ozguryazilim.telve.nav;

import com.google.common.base.Strings;
import com.ozguryazilim.mutfak.kahve.Kahve;
import com.ozguryazilim.mutfak.kahve.KahveEntry;
import com.ozguryazilim.mutfak.kahve.annotations.UserAware;
import com.ozguryazilim.telve.auth.SecuredPage;
import com.ozguryazilim.telve.feature.FeatureHandler;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.NavigationHandler;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session başına navigosyan itemlarını belirler ve gerektiğinde sunar.
 *
 * DeltaSpike viewConfig üzerinden Navigation ile işaretlenmiş olan bilgileri
 * SecuredPages üzerinde kontrol ederek kullanıcı ( session ) başına oluşturup
 * saklar.
 *
 * @author Hakan Uygun
 */
@SessionScoped
@Named
public class NagivationController implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(NagivationController.class);

    @Inject
    private ViewConfigResolver viewConfigResolver;
    
    @Inject @UserAware
    private Kahve kahve;

    private List<NavigationLinkModel> mainNavigations = new ArrayList<>();
    private List<NavigationLinkModel> sideNavigations = new ArrayList<>();
    private List<NavigationLinkModel> favNavigations = new ArrayList<>();
    private List<NavigationSection> navigationSections = new ArrayList<>();
    private Map<String, List<NavigationLinkModel>> navigations = new HashMap<>();

    /**
     * Session için navigasyon ağacını toparlar.
     */
    @PostConstruct
    public void init() {
        try {
            //NavigationOptionPane save sırasında init'i yeniden çapırıp cache temizliyor.
            mainNavigations.clear();
            sideNavigations.clear();
            favNavigations.clear();
            navigationSections.clear();
            navigations.clear();
            buildNav();
        } catch (InstantiationException | IllegalAccessException ex) {
            LOG.error("NavigationError", ex);
        }
    }

    protected void buildNav() throws InstantiationException, IllegalAccessException {
        Subject identity = SecurityUtils.getSubject();
        
        //Favori listesini bir alalım
        List<String> favMenus = loadFavNav();
            
        for (ViewConfigDescriptor vi : viewConfigResolver.getViewConfigDescriptors()) {

            //Yetki kontrolü yapıyoruz. Eğer erişim yetkisi yoksa bahsi geçen View için nav oluşturulmayacak.
            List<SecuredPage> sec = vi.getMetaData(SecuredPage.class);
            if (!sec.isEmpty()) {
                SecuredPage sc = sec.get(0);
                if (!Strings.isNullOrEmpty(sc.value())) {
                    if (!identity.isPermitted(sc.value() + ":select")) {
                        continue;
                    }
                    //Yetki çıkarma kontrolü
                    if( "true".equals(ConfigResolver.getPropertyValue("permission.exclude." + sc.value(), "false"))){
                        continue;
                    }
                }
            }

            //Navigation işaretlerini topla
            List<Navigation> ls = vi.getMetaData(Navigation.class);

            //Eğer birden fazla navigasyon tanımı varsa onları da toparlıyoruz.
            List<Navigations> lss = vi.getMetaData(Navigations.class);
            for (Navigations nvs : lss) {
                ls.addAll(Arrays.asList(nvs.value()));
            }

            
            
            //Şimdi gerekli modeller build ediliyor
            for (Navigation nav : ls) {
                
                String label;
                String icon;
                
                if( Strings.isNullOrEmpty(nav.label()) ){
                    if( !nav.feature().equals(FeatureHandler.class) ){
                        label = "feature.plural.caption." + nav.feature().getSimpleName();
                    } else {
                        label = "nav.label." + vi.getConfigClass().getSimpleName();
                    }
                } else {
                    label = nav.label();
                }
                
                if( Strings.isNullOrEmpty(nav.icon()) ){
                    if( !nav.feature().getSimpleName().equals(NavigationHandler.class.getSimpleName()) ){
                        icon = "feature.icon." + nav.feature().getSimpleName();
                    } else {
                        icon = "nav.icon." + vi.getConfigClass().getSimpleName();
                    }
                } else {
                    icon = nav.icon();
                }
                
                //Link Model oluştu
                NavigationLinkModel nlm = new NavigationLinkModel(vi.getViewId(), label, icon, nav.order());

                //Şimdi yerine koyalım
                if (nav.section() == MainNavigationSection.class) {
                    mainNavigations.add(nlm);
                } else if (nav.section() == SideNavigationSection.class) {
                    sideNavigations.add(nlm);
                } else {
                    NavigationSection ns = findNavigationSection(nav.section());
                    List<NavigationLinkModel> navs = navigations.get(ns.getLabel());
                    if (navs == null) {
                        navs = new ArrayList<>();
                        navigations.put(ns.getLabel(), navs);
                    }
                    navs.add(nlm);
                }
                
                //Favorilerde ise favoriye de ekleyelim
                if( favMenus.contains(label)){
                    //Eğer ki daha önce eklenmemiş ise. Navlar sectionlarda bir kaç kere tanımlana biliyor.
                    if( !favNavigations.contains(nlm)){
                        favNavigations.add(nlm);
                    }
                }

            }
        }

        //Sıralamaları yapalım.
        Collections.sort(mainNavigations);
        Collections.sort(sideNavigations);
        Collections.sort(navigationSections);
        
        //Favorileri zaten kullanıcı sıraladı. Onun kurallarına uyalım
        Collections.sort(favNavigations, (NavigationLinkModel t, NavigationLinkModel t1) -> {
            Integer ix1 = favMenus.indexOf(t.getLabel());
            Integer ix2 = favMenus.indexOf(t1.getLabel());
            return ix1.compareTo(ix2);
        });
        
        //Map içindeki listeleri sıralıyoruz.
        navigations.entrySet().forEach((e) -> {
            Collections.sort(e.getValue());
        });
    
        LOG.debug("Main Nav : {}", mainNavigations);
        LOG.debug("Side Nav : {}", sideNavigations);
        LOG.debug("Other Nav : {}", navigations);
    }

    /**
     * Favori olarka seçilen menüleri oluşturur.
     * @return Favori menulerin label listesi
     */
    protected List<String> loadFavNav(){
        
        List<String> menus = new ArrayList<>();
        
        //Önce Kahveden değerleri okuyalım
        KahveEntry ke = kahve.get("favmenu.count", 0);
        int count = ke.getAsInteger();
        for( int i = 0; i < count; i++ ){
            ke = kahve.get("favmenu.item." + i, "");
            menus.add(ke.getAsString());
        }
        
        return menus;
    }
    
    /**
     * Lİsteye bakıp navigation section bulur. 
     * 
     * Bulamazsa yeni bir instance oluşturup listeye ekler.
     * 
     * @param clazz
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    private NavigationSection findNavigationSection( Class< ? extends  NavigationSection> clazz ) throws InstantiationException, IllegalAccessException{
        for( NavigationSection ns : navigationSections ){
            if( ns.getClass() == clazz ) {
                return ns;
            }
        }
        
        NavigationSection ns = clazz.newInstance();
        navigationSections.add(ns);
        return ns;
    }
    
    public List<NavigationLinkModel> getMainNavigations() {
        return mainNavigations;
    }

    public void setMainNavigations(List<NavigationLinkModel> mainNavigations) {
        this.mainNavigations = mainNavigations;
    }

    public List<NavigationLinkModel> getSideNavigations() {
        return sideNavigations;
    }

    public void setSideNavigations(List<NavigationLinkModel> sideNavigations) {
        this.sideNavigations = sideNavigations;
    }

    public Map<String, List<NavigationLinkModel>> getNavigations() {
        return navigations;
    }

    /**
     * Geriye section key'lerini barındıran Set döndürür.
     *
     * @return
     */
    public List<NavigationSection> getNavigationSections() {
        return navigationSections;
    }

    /**
     * Geriye ismi verilen section'ın itemlarını barındıran liste döndürür.
     *
     * @param section
     * @return
     */
    public List<NavigationLinkModel> getSectionLinks(String section) {
        LOG.debug("Nav request for {}", section);
        return navigations.get(section);
    }

    public List<NavigationLinkModel> getFavNavigations() {
        return favNavigations;
    }

    
}
