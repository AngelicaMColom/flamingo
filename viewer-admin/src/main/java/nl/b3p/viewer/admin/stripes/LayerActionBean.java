/*
 * Copyright (C) 2011-2015 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.viewer.admin.stripes;

import java.util.*;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import nl.b3p.viewer.config.ClobElement;
import nl.b3p.viewer.config.app.*;
import nl.b3p.viewer.config.security.Group;
import nl.b3p.viewer.config.services.*;
import nl.b3p.viewer.util.SelectedContentCache;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Jytte Schaeffer
 */
@UrlBinding("/action/layer")
@StrictBinding
@RolesAllowed({Group.ADMIN,Group.REGISTRY_ADMIN})
public class LayerActionBean implements ActionBean {

    private static final String JSP = "/WEB-INF/jsp/services/layer.jsp";
    private static final String DEFAULT_LEVEL_NAME = "Toegevoegde lagen";
    
    private ActionBeanContext context;
    @Validate
    @ValidateNestedProperties({
        @Validate(field = "titleAlias", label="Naam"),
        @Validate(field = "legendImageUrl", label="Legenda"),
        @Validate(field = "exception_type")
    })
    private Layer layer;
    @Validate
    private String parentId;
    private List<Group> allGroups;
    private SortedSet<String> applicationsUsedIn = new TreeSet();
    @Validate
    private List<String> groupsRead = new ArrayList<String>();
    @Validate
    private List<String> groupsWrite = new ArrayList<String>();
    @Validate
    private List<String> groupsPreventGeomEdit = new ArrayList<String>();
    @Validate
    private Map<String, String> details = new HashMap<String, String>();
    @Validate
    private SimpleFeatureType simpleFeatureType;
    @Validate
    private Long featureSourceId;
    private List featureSources;
    
    @Validate(required = true, on = "addLayerToApplication")
    private Application application;
    
    //<editor-fold defaultstate="collapsed" desc="getters & setters">
    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }

    public Layer getLayer() {
        return layer;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public List<Group> getAllGroups() {
        return allGroups;
    }

    public void setAllGroups(List<Group> allGroups) {
        this.allGroups = allGroups;
    }

    public List<String> getGroupsRead() {
        return groupsRead;
    }

    public void setGroupsRead(List<String> groupsRead) {
        this.groupsRead = groupsRead;
    }

    public List<String> getGroupsWrite() {
        return groupsWrite;
    }

    public void setGroupsWrite(List<String> groupsWrite) {
        this.groupsWrite = groupsWrite;
    }

    public List<String> getGroupsPreventGeomEdit() {
        return groupsPreventGeomEdit;
    }

    public void setGroupsPreventGeomEdit(List<String> groupsPreventGeomEdit) {
        this.groupsPreventGeomEdit = groupsPreventGeomEdit;
    }

    public SortedSet<String> getApplicationsUsedIn() {
        return applicationsUsedIn;
    }

    public void setApplicationsUsedIn(SortedSet<String> applicationsUsedIn) {
        this.applicationsUsedIn = applicationsUsedIn;
    }

    public SimpleFeatureType getSimpleFeatureType() {
        return simpleFeatureType;
    }

    public void setSimpleFeatureType(SimpleFeatureType simpleFeatureType) {
        this.simpleFeatureType = simpleFeatureType;
    }

    public Long getFeatureSourceId() {
        return featureSourceId;
    }

    public void setFeatureSourceId(Long featureSourceId) {
        this.featureSourceId = featureSourceId;
    }

    public List getFeatureSources() {
        return featureSources;
    }

    public void setFeatureSources(List featureSources) {
        this.featureSources = featureSources;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    //</editor-fold>

    @Before(stages = LifecycleStage.BindingAndValidation)
    @SuppressWarnings("unchecked")
    public void load() {
        allGroups = Stripersist.getEntityManager().createQuery("from Group").getResultList();
        featureSources = Stripersist.getEntityManager().createQuery("from FeatureSource").getResultList();
    }

    @DefaultHandler
    public Resolution edit() {
        if (layer != null) {
            details = new HashMap();
            for(Map.Entry<String,ClobElement> e: layer.getDetails().entrySet()) {
                details.put(e.getKey(), e.getValue().getValue());
            }

            groupsRead.addAll(layer.getReaders());
            groupsWrite.addAll(layer.getWriters());
            groupsPreventGeomEdit.addAll(layer.getPreventGeomEditors());

            if (layer.getFeatureType() != null) {
                simpleFeatureType = layer.getFeatureType();
                featureSourceId = simpleFeatureType.getFeatureSource().getId();
            }

            findApplicationsUsedIn();
        }
        return new ForwardResolution(JSP);
    }

    private void findApplicationsUsedIn() {
        List<Application> applications = findApplications();
        for (Application application : applications) {
            applicationsUsedIn.add(application.getNameWithVersion());
        }
    }
    
    private List<Application> findApplications() {
        List<Application> apps = new ArrayList();
        GeoService service = layer.getService();
        String layerName = layer.getName();

        List<ApplicationLayer> applicationLayers = Stripersist.getEntityManager().createQuery("from ApplicationLayer where service = :service"
                + " and layerName = :layerName").setParameter("service", service).setParameter("layerName", layerName).getResultList();

        for (Iterator it = applicationLayers.iterator(); it.hasNext();) {
            ApplicationLayer appLayer = (ApplicationLayer) it.next();

            /*
             * The parent level of the applicationLayer is needed to find out in
             * which application the Layer is used. This solution is not good
             * when there are many levels.
             */
            List<Application> applications = Stripersist.getEntityManager().createQuery("from Application").getResultList();
            for (Application app : applications) {
                if (app.getRoot().containsLayerInSubtree(appLayer)) {
                    apps.add(app);
                }
            }
        }
        return apps;
    }

    public Resolution save() {
        // Only remove details which are editable and re-added layer if not empty,
        // retain other details (for example "wms.styles")
        // See JSP for which keys are edited 
        layer.getDetails().keySet().removeAll(Arrays.asList(
                Layer.EXTRA_KEY_METADATA_STYLESHEET_URL,
                Layer.EXTRA_KEY_DOWNLOAD_URL,
                Layer.EXTRA_KEY_FILTERABLE,
                Layer.DETAIL_ALTERNATE_LEGEND_IMAGE_URL
        ));
        for(Map.Entry<String,String> e: details.entrySet()) {
            if(e.getValue() != null) { // Don't insert null value ClobElement 
                layer.getDetails().put(e.getKey(), new ClobElement(e.getValue()));
            }
        }

        layer.getReaders().clear();
        for (String groupName : groupsRead) {
            layer.getReaders().add(groupName);
        }

        layer.getWriters().clear();
        for (String groupName : groupsWrite) {
            layer.getWriters().add(groupName);
        }

        layer.getPreventGeomEditors().clear();
        for (String groupName : groupsPreventGeomEdit) {
            layer.getPreventGeomEditors().add(groupName);
        }

        layer.setFeatureType(simpleFeatureType);

        Stripersist.getEntityManager().persist(layer);
        layer.getService().authorizationsModified();
        List<Application> apps = findApplications();
        EntityManager em = Stripersist.getEntityManager();
        for (Application application : apps) {
            SelectedContentCache.setApplicationCacheDirty(application, true, false, em);
        }
        Stripersist.getEntityManager().getTransaction().commit();
        getContext().getMessages().add(new SimpleMessage("De kaartlaag is opgeslagen"));

        return new ForwardResolution(JSP);
    }
    
    
    public Resolution addLayerToApplication(){
        EntityManager em = Stripersist.getEntityManager();
        application.loadTreeCache(em);
        List<Level> levels = application.getTreeCache().getLevels();
        Level defaultLevel = null;
        // check of default mapje bestaat
            // zo nee, aanmaken
            // zo ja, ophalen
        for (Level level : levels) {
            if(level.getName().equals(DEFAULT_LEVEL_NAME)){
                defaultLevel = level;
            }
        }
        if(defaultLevel == null){
            Level parent = new Level();
            parent.setName(DEFAULT_LEVEL_NAME + " ouder");
            parent.setParent(application.getRoot());
            application.getRoot().getChildren().add(parent);
            em.persist(parent);
            em.persist(application.getRoot());
            
            
            defaultLevel = new Level();
            defaultLevel.setName(DEFAULT_LEVEL_NAME);
            parent.getChildren().add(defaultLevel);
            defaultLevel.setParent(parent);
            em.persist(defaultLevel);
            em.persist(parent);
        }        
        //voeg laag toe aan applicatie:
            //ApplicationTreeLevelActionBean.save
        ApplicationTreeLevelActionBean atlab = new ApplicationTreeLevelActionBean();
        atlab.setContext(context);
        atlab.setApplication(application);
        atlab.setLevel(defaultLevel);
        atlab.setSelectedlayers("l" + layer.getId());
        String docsString = "";
        List<Document> docs = defaultLevel.getDocuments();
        for (int i = 0; i < docs.size(); i++) {
            Document doc = docs.get(i);
            if(i > 0){
                docsString += ",";
            }
            docsString += doc.getId();
        }
        atlab.setSelecteddocs(docsString);
        atlab.save();
        return new ForwardResolution(JSP);
    }
}
