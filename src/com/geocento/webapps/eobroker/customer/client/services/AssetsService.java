package com.geocento.webapps.eobroker.customer.client.services;

import com.geocento.webapps.eobroker.common.shared.AuthorizationException;
import com.geocento.webapps.eobroker.common.shared.entities.DatasetAccessOGC;
import com.geocento.webapps.eobroker.common.shared.entities.ImageService;
import com.geocento.webapps.eobroker.common.shared.entities.NewsItem;
import com.geocento.webapps.eobroker.common.shared.entities.dtos.AoIDTO;
import com.geocento.webapps.eobroker.common.shared.entities.dtos.CompanyDTO;
import com.geocento.webapps.eobroker.customer.shared.ProductDTO;
import com.geocento.webapps.eobroker.customer.shared.*;
import com.google.gwt.http.client.RequestException;
import org.fusesource.restygwt.client.DirectRestService;

import javax.ws.rs.*;
import java.util.List;

public interface AssetsService extends DirectRestService {

    @GET
    @Path("/assets/aoi/")
    @Produces("application/json")
    public List<AoIDTO> listAoIs() throws AuthorizationException, RequestException;

    @GET
    @Path("/assets/aoi/{id}")
    @Produces("application/json")
    public AoIDTO getAoI(@PathParam("id") Long id) throws RequestException;

    @GET
    @Path("/assets/aoi/latest")
    @Produces("application/json")
    public AoIDTO loadLatestAoI() throws RequestException;

    @DELETE
    @Path("/assets/aoi/latest")
    @Produces("application/json")
    public void removeLatestAoI() throws RequestException;

    @PUT
    @Path("/assets/aoi/")
    @Produces("application/json")
    public AoIDTO updateAoI(AoIDTO aoi) throws RequestException;

    @POST
    @Path("/assets/aoi/{id}")
    void updateAoIName(@PathParam("id") Long id, @QueryParam("name") String name) throws RequestException;

    @DELETE
    @Path("/assets/aoi/{id}")
    public void deleteAoI(@PathParam("id") Long id) throws RequestException;

    @GET
    @Path("/assets/product/{id}")
    @Produces("application/json")
    public ProductDTO getProduct(@PathParam("id") Long id) throws RequestException;

    @GET
    @Path("/assets/product/filters/{id}")
    @Produces("application/json")
    ProductWithFiltersDTO getProductWithFilters(@PathParam("id") Long id) throws RequestException;

    @GET
    @Path("/assets/challenges/{id}")
    @Produces("application/json")
    public ChallengeDescriptionDTO getChallengeDescription(@PathParam("id") Long challengeId) throws RequestException;

    @GET
    @Path("/assets/product/{id}/feasibility/")
    @Produces("application/json")
    public ProductFeasibilityDTO getProductFeasibility(@PathParam("id") Long id) throws RequestException;

    @GET
    @Path("/assets/product/form/{id}")
    @Produces("application/json")
    ProductFormDTO getProductForm(@PathParam("id") Long productId) throws RequestException;

    @GET
    @Path("/assets/company/description/{id}")
    @Produces("application/json")
    CompanyDescriptionDTO getCompanyDescription(@PathParam("id") Long companyId) throws RequestException;

    @GET
    @Path("/assets/company/{id}")
    @Produces("application/json")
    CompanyDTO getCompany(@PathParam("id") Long companyId) throws RequestException;

    @GET
    @Path("/assets/company/")
    @Produces("application/json")
    CustomerCompanyDTO getUserCompany() throws RequestException;

    @PUT
    @Path("/assets/company/")
    @Produces("application/json")
    void updateUserCompany(CustomerCompanyDTO companyDTO) throws RequestException;

    @GET
    @Path("/assets/productservices/description/{id}")
    @Produces("application/json")
    ProductServiceDescriptionDTO getProductServiceDescription(@PathParam("id") Long productServiceId) throws RequestException;

    @GET
    @Path("/assets/product/description/{id}")
    @Produces("application/json")
    ProductDescriptionDTO getProductDescription(@PathParam("id") Long productId) throws RequestException;

    @GET
    @Path("/assets/productdataset/description/{id}")
    @Produces("application/json")
    ProductDatasetDescriptionDTO getProductDatasetDescription(@PathParam("id") Long productDatasetId) throws RequestException;

    @GET
    @Path("/assets/productdatasets/{id}/catalogue")
    @Produces("application/json")
    ProductDatasetCatalogueDTO getProductDatasetCatalogueDTO(@PathParam("id") Long productId) throws RequestException;

    @GET
    @Path("/assets/software/description/{id}")
    @Produces("application/json")
    SoftwareDescriptionDTO getSoftwareDescription(@PathParam("id") Long softwareId) throws RequestException;

    @GET
    @Path("/assets/project/description/{id}")
    @Produces("application/json")
    ProjectDescriptionDTO getProjectDescription(@PathParam("id") Long projectId) throws RequestException;

    @GET
    @Path("/assets/productdatasets/visualisation/{id}")
    @Produces("application/json")
    ProductDatasetVisualisationDTO getProductDatasetVisualisation(@PathParam("id") Long id) throws RequestException;

    @GET
    @Path("/assets/productservice/visualisation/{id}")
    @Produces("application/json")
    ProductServiceVisualisationDTO getProductServiceVisualisation(@PathParam("id") Long id) throws RequestException;

    @GET
    @Path("/assets/imageservices")
    @Produces("application/json")
    List<ImageService> getImageServices() throws RequestException;

    @GET
    @Path("/assets/newsitems")
    @Produces("application/json")
    List<NewsItem> getNewsItems() throws RequestException;

    @GET
    @Path("/assets/notifications/")
    @Produces("application/json")
    List<NotificationDTO> listNotifications(@QueryParam("start") int start, @QueryParam("limit") int limit) throws RequestException;

    @GET
    @Path("/assets/notifications/{id}")
    @Produces("application/json")
    NotificationDTO getNotification(@PathParam("id") Long id) throws RequestException;

    @GET
    @Path("/assets/spatial/{id}")
    @Produces("application/json")
    LayerInfoDTO getLayerInfo(@PathParam("id") Long id) throws RequestException;

    @PUT
    @Path("/assets/company/{id}/follow")
    Long followCompany(@PathParam("id") Long companyId, Boolean follow) throws RequestException;

    @PUT
    @Path("/assets/product/{id}/follow")
    Long followProduct(@PathParam("id") Long productId, Boolean follow) throws RequestException;

    @PUT
    @Path("/assets/service/{id}/follow")
    Boolean followProductService(@PathParam("id") Long serviceId, Boolean follow) throws RequestException;

    @PUT
    @Path("/assets/dataset/{id}/follow")
    Boolean followProductDataset(@PathParam("id") Long productDatasetId, Boolean follow) throws RequestException;

    @PUT
    @Path("/assets/software/{id}/follow")
    Boolean followSoftware(@PathParam("id") Long softwareId, Boolean follow) throws RequestException;

    @PUT
    @Path("/assets/project/{id}/follow")
    Boolean followProject(@PathParam("id") Long projectId, Boolean follow) throws RequestException;

    @GET
    @Path("/assets/follow/events")
    List<FollowingEventDTO> getFollowingEvents(@QueryParam("start") int start, @QueryParam("limit") int limit) throws RequestException;

    @GET
    @Path("/assets/testimonials")
    @Produces("application/json")
    List<TestimonialDTO> listTestimonials() throws RequestException;

    @GET
    @Path("/assets/testimonials/{id}")
    @Produces("application/json")
    TestimonialDTO getTestimonial(@PathParam("id") Long id) throws RequestException;

    @POST
    @Path("/assets/testimonials")
    Long createTestimonial(TestimonialDTO testimonialDTO) throws RequestException;

    @PUT
    @Path("/assets/testimonials")
    void updateTestimonial(TestimonialDTO testimonialDTO) throws RequestException;

    @DELETE
    @Path("/assets/testimonials/{id}")
    void deleteTestimonial(@PathParam("id") Long id) throws RequestException;

    @GET
    @Path("/assets/service/{id}/form")
    @Produces("application/json")
    ProductServiceFormDTO getProductServiceForm(@PathParam("id") Long productServiceId) throws RequestException;

    @GET
    @Path("/assets/service/search/{id}")
    @Produces("application/json")
    ProductServiceSearchFormDTO getProductServiceSearchForm(@PathParam("id") String searchId) throws RequestException;

    @POST
    @Path("/assets/company/create")
    @Produces("application/json")
    @Consumes("application/json")
    CompanyDTO createCompany(CreateCompanyDTO createCompanyDTO) throws RequestException;

    @POST
    @Path("/assets/user/create")
    @Consumes("application/json")
    void createUser(CreateUserDTO createUserDTO) throws RequestException;

    @GET
    @Path("/assets/user/settings")
    @Produces("application/json")
    SettingsDTO getUserSettings() throws RequestException;

    @POST
    @Path("/assets/user/settings")
    @Consumes("application/json")
    void saveUserSettings(SettingsDTO settingsDTO) throws RequestException;

    @GET
    @Path("/assets/user/layers/selected")
    @Produces("application/json")
    List<DatasetAccessOGC> getSelectedLayers() throws RequestException;

    @POST
    @Path("/assets/user/layers/selected/{id}")
    @Consumes("application/json")
    void addSelectedLayer(@PathParam("id") Long layerId) throws RequestException;

    @DELETE
    @Path("/assets/user/layers/selected/{id}")
    @Consumes("application/json")
    void removeSelectedLayer(@PathParam("id") Long layerId) throws RequestException;

    @GET
    @Path("/assets/user/layers/company/saved")
    @Produces("application/json")
    List<DatasetAccessOGC> getCompanySavedLayers() throws RequestException;

/*
    @POST
    @Path("/assets/user/layers/company/saved")
    @Consumes("application/json")
    void addCompanySavedLayer(DatasetAccessOGC datasetAccessOGC) throws RequestException;

    @DELETE
    @Path("/assets/user/layers/company/{id}")
    @Consumes("application/json")
    void removeCompanyLayer(@PathParam("id") Long layerId) throws RequestException;
*/

    @GET
    @Path("/assets/user/layers/application")
    @Produces("application/json")
    List<DatasetAccessOGC> getApplicationLayers() throws RequestException;

    @GET
    @Path("/assets/user/layers/saved")
    @Produces("application/json")
    List<DatasetAccessOGC> getSavedLayers() throws RequestException;

    @POST
    @Path("/assets/user/layers/saved/{id}")
    @Produces("application/json")
    void addSavedLayer(@PathParam("id") Long layerId) throws RequestException;

    @DELETE
    @Path("/assets/user/layers/saved/{id}")
    @Produces("application/json")
    void removeSavedLayer(@PathParam("id") Long layerId) throws RequestException;

    @GET
    @Path("/successstories/{id}")
    @Produces("application/json")
    SuccessStoryDTO getSuccessStory(@PathParam("id") Long successStoryId) throws RequestException;

    @POST
    @Path("/assets/user/invite/")
    @Produces("application/json")
    void inviteColleague(String email) throws RequestException;

    @POST
    @Path("/assets/conversations/subscribe/")
    Boolean subscribeCompanyMessages(String companyId) throws RequestException;

}
