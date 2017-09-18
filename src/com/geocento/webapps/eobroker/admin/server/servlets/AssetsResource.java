package com.geocento.webapps.eobroker.admin.server.servlets;

import com.geocento.webapps.eobroker.admin.client.services.AssetsService;
import com.geocento.webapps.eobroker.admin.server.util.UserUtils;
import com.geocento.webapps.eobroker.admin.shared.dtos.*;
import com.geocento.webapps.eobroker.common.server.EMF;
import com.geocento.webapps.eobroker.common.server.MailContent;
import com.geocento.webapps.eobroker.common.server.ServerUtil;
import com.geocento.webapps.eobroker.common.server.Utils.DBHelper;
import com.geocento.webapps.eobroker.common.server.Utils.GeoserverUtils;
import com.geocento.webapps.eobroker.common.server.Utils.KeyGenerator;
import com.geocento.webapps.eobroker.common.shared.entities.*;
import com.geocento.webapps.eobroker.common.shared.entities.datasets.DatasetProvider;
import com.geocento.webapps.eobroker.common.shared.entities.dtos.AoIDTO;
import com.geocento.webapps.eobroker.common.shared.entities.dtos.AoIPolygonDTO;
import com.geocento.webapps.eobroker.common.shared.entities.dtos.CompanyDTO;
import com.geocento.webapps.eobroker.common.shared.entities.formelements.FormElement;
import com.geocento.webapps.eobroker.common.shared.entities.notifications.AdminNotification;
import com.geocento.webapps.eobroker.common.shared.entities.utils.CompanyHelper;
import com.geocento.webapps.eobroker.common.shared.utils.ListUtil;
import com.google.gwt.http.client.RequestException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Path("/")
public class AssetsResource implements AssetsService {

    static Logger logger = Logger.getLogger(AssetsResource.class);

    // assumes service is not a singleton
    @Context
    HttpServletRequest request;

    public AssetsResource() {
        logger.info("Starting service...");
    }

    @Override
    public AoIDTO getAoI(Long id) {
        return new AoIPolygonDTO();
    }

    @Override
    public Long addAoI(AoIDTO aoi) {
        return null;
    }

    @Override
    public void updateAoI(AoIDTO aoi) {

    }

    @Override
    public Long updateProduct(EditProductDTO productDTO) throws RequestException {
        UserUtils.verifyUserAdmin(request);
        if(productDTO == null) {
            throw new RequestException("Product is required");
        }
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            Product product = null;
            if (productDTO.getId() != null) {
                product = em.find(Product.class, productDTO.getId());
                if (product == null) {
                    throw new RequestException("Could not find product with id '" + productDTO.getId() + "'");
                }
            } else {
                product = new Product();
            }
            product.setName(productDTO.getName());
            product.setImageUrl(productDTO.getImageUrl());
            product.setShortDescription(productDTO.getShortDescription());
            product.setDescription(productDTO.getDescription());
            product.setSector(productDTO.getSector());
            product.setThematic(productDTO.getThematic());
            product.setGeoinformation(productDTO.getGeoinformation());
            for(FeatureDescription featureDescription : productDTO.getGeoinformation()) {
                if(featureDescription.getId() == null) {
                    em.persist(featureDescription);
                }
            }
            product.setPerformances(productDTO.getPerformances());
            for(PerformanceDescription performanceDescription : productDTO.getPerformances()) {
                if(performanceDescription.getId() == null) {
                    em.persist(performanceDescription);
                }
            }
            product.setFormFields(productDTO.getFormFields());
            for(FormElement formElement : product.getFormFields()) {
                if(formElement.getId() == null) {
                    em.persist(formElement);
                }
            }
            product.setApiFormFields(productDTO.getApiFormFields());
            for(FormElement formElement : product.getApiFormFields()) {
                if(formElement.getId() == null) {
                    em.persist(formElement);
                }
            }
            product.setRecommendationRule(productDTO.getRecommendationRule());
            if(product.getId() == null) {
                em.persist(product);
            } else {
                em.merge(product);
            }
            // update the keyphrases
            Query query = em.createNativeQuery("UPDATE product SET tsv = " + DBHelper.getProductTSV(product) +
                    ", tsvname = " + DBHelper.getProductNameTSV(product) + " where id = " + product.getId() +
                    ";");
            query.executeUpdate();
            em.getTransaction().commit();
            return product.getId();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if(em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RequestException("Server error");
        } finally {
            em.close();
        }
    }

    @Override
    public EditProductDTO getProduct(Long id) throws RequestException {
        UserUtils.verifyUserAdmin(request);
        if(id == null) {
            throw new RequestException("Id cannot be null");
        }
        EntityManager em = EMF.get().createEntityManager();
        try {
            Product product = em.find(Product.class, id);
            if(product == null) {
                throw new RequestException("Unknown product");
            }
            EditProductDTO productDTO = new EditProductDTO();
            productDTO.setId(product.getId());
            productDTO.setName(product.getName());
            productDTO.setShortDescription(product.getShortDescription());
            productDTO.setDescription(product.getDescription());
            productDTO.setImageUrl(product.getImageUrl());
            productDTO.setSector(product.getSector());
            productDTO.setThematic(product.getThematic());
            productDTO.setFormFields(product.getFormFields());
            productDTO.setApiFormFields(product.getApiFormFields());
            productDTO.setRecommendationRule(product.getRecommendationRule());
            productDTO.setGeoinformation(product.getGeoinformation());
            productDTO.setPerformances(product.getPerformances());
            return productDTO;
        } catch (Exception e) {
            throw new RequestException("Error");
        } finally {
            em.close();
        }
    }

    @Override
    public List<ProductDTO> listProducts(int start, int limit, String orderBy, String filter) throws RequestException {
        UserUtils.verifyUserAdmin(request);
        EntityManager em = EMF.get().createEntityManager();
        try {
            if(orderBy == null) {
                orderBy = "p.name";
            } else {
                switch(orderBy) {
                    case "name":
                        orderBy = "p.name";
                        break;
                    default:
                        orderBy = "p.name";
                }
            }
            boolean hasFilter = filter != null && filter.length() > 0;
            TypedQuery<Product> query = em.createQuery("select p from Product p" +
                            (hasFilter ?  "  where UPPER(p.name) LIKE UPPER(:filter)" : "") +
                            " order by " + orderBy, Product.class);
            if(hasFilter) {
                query.setParameter("filter", "%" + filter + "%");
            }
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return ListUtil.mutate(query.getResultList(), new ListUtil.Mutate<Product, ProductDTO>() {
                @Override
                public ProductDTO mutate(Product product) {
                    return ProductHelper.createProductDTO(product);
                }
            });
        } catch (Exception e) {
            throw new RequestException("Error");
        } finally {
            em.close();
        }
    }

    @Override
    public void removeProduct(Long id) throws RequestException {
        UserUtils.verifyUserAdmin(request);
        if(id == null) {
            throw new RequestException("Id cannot be null");
        }
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            Product product = em.find(Product.class, id);
            if(product == null) {
                throw new RequestException("Unknown product");
            }
            em.remove(product);
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new RequestException("Error");
        } finally {
            em.close();
        }
    }

    @Override
    public CompanyDTO getCompany(Long id) throws RequestException {
        String userName = UserUtils.verifyUserAdmin(request);
        EntityManager em = EMF.get().createEntityManager();
        try {
            if (id == null) {
                throw new RequestException("Missing company id");
            }
            Company company = em.find(Company.class, id);
            if (company == null) {
                throw new RequestException("Unknown company");
            }
            CompanyDTO companyDTO = CompanyHelper.createFullCompanyDTO(company);
            companyDTO.setStatus(company.getStatus());
            return companyDTO;
        } catch (Exception e) {
            if(em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error(e.getMessage(), e);
            throw new RequestException("Issue retrieving company");
        } finally {
            em.close();
        }
    }

    @Override
    public List<CompanyDTO> listCompanies(int start, int limit, String orderby, String filter) throws RequestException {
        String userName = UserUtils.verifyUserAdmin(request);
        EntityManager em = EMF.get().createEntityManager();
        try {
            boolean hasFilter = filter != null && filter.length() > 0;
            // force orderby if null
            if(orderby == null) {
                orderby = "creationDate";
            }
            switch(orderby) {
                case "creationDate":
                    orderby = "c.name";
                    break;
                default:
                    orderby = "c.name";
            }
            TypedQuery<Company> query = em.createQuery("select c from Company c" +
                    (hasFilter ?  "  where UPPER(c.name) LIKE UPPER(:filter)" : "") +
                    " order by " + orderby, Company.class);
            if(hasFilter) {
                query.setParameter("filter", "%" + filter + "%");
            }
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return ListUtil.mutate(query.getResultList(), new ListUtil.Mutate<Company, CompanyDTO>() {
                @Override
                public CompanyDTO mutate(Company company) {
                    CompanyDTO companyDTO = CompanyHelper.createCompanyDTO(company);
                    companyDTO.setStatus(company.getStatus());
                    companyDTO.setSupplier(company.isSupplier());
                    return companyDTO;
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RequestException("Server error");
        } finally {
            em.close();
        }
    }

/*
    @Override
    public void updateCompany(CompanyDTO companyDTO) throws RequestException {
        String userName = UserUtils.verifyUserAdmin(request);
        if(companyDTO == null || companyDTO.getId() == null) {
            throw new RequestException("Company cannot be null");
        }
        EntityManager em = EMF.get().createEntityManager();
        Company company = em.find(Company.class, companyDTO.getId());
        if(company == null) {
            throw new RequestException("Unknown company");
        }
        User user = em.find(User.class, userName);
        if(user.getCompany().getId().compareTo(companyDTO.getId()) != 0) {
            throw new AuthorizationException();
        }
        try {
            em.getTransaction().begin();
            company.setName(companyDTO.getName());
            company.setDescription(companyDTO.getDescription());
            company.setWebsite(companyDTO.getWebsite());
            company.setContactEmail(companyDTO.getContactEmail());
            company.setFullDescription(companyDTO.getFullDescription());
            company.setIconURL(companyDTO.getIconURL());
            em.getTransaction().commit();
        } catch (Exception e) {
            if(em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error(e.getMessage(), e);
            throw new RequestException("Error updating company");
        } finally {
            em.close();
        }
    }
*/

    // TODO - replace with specific CompanyDTO
    @Override
    public Long saveCompany(CompanyDTO companyDTO) throws RequestException {
        String userName = UserUtils.verifyUserAdmin(request);
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            Company dbCompany = null;
            if(companyDTO.getId() == null) {
                dbCompany = new Company();
            } else {
                dbCompany = em.find(Company.class, companyDTO.getId());
                if (dbCompany == null) {
                    throw new RequestException("Could not find news item to update");
                }
            }
            dbCompany.setName(companyDTO.getName());
            dbCompany.setSupplier(companyDTO.isSupplier());
            dbCompany.setStatus(companyDTO.getStatus());
            dbCompany.setDescription(companyDTO.getDescription());
            dbCompany.setWebsite(companyDTO.getWebsite());
            dbCompany.setIconURL(companyDTO.getIconURL());
            dbCompany.setFullDescription(companyDTO.getFullDescription());
            dbCompany.setContactEmail(companyDTO.getContactEmail());
            if(companyDTO.getId() == null) {
                em.persist(dbCompany);
                // if new company create workspace
                GeoserverUtils.getGeoserverPublisher().createWorkspace(dbCompany.getId() + "");
            } else {
/*
                em.merge(dbCompany);
*/
            }
            // update the keyphrases
            Query query = em.createNativeQuery("UPDATE company SET tsv = " + DBHelper.getCompanyTSV(dbCompany) +
                    ", tsvname = " + DBHelper.getCompanyNameTSV(dbCompany) + " where id = " + dbCompany.getId() +
                    ";");
            query.executeUpdate();
            em.getTransaction().commit();
            return dbCompany.getId();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if(em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RequestException("Server error");
        } finally {
            em.close();
        }
    }

    @Override
    public void approveCompany(Long id) throws RequestException {
        String userName = UserUtils.verifyUserAdmin(request);
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            Company dbCompany = null;
            dbCompany = em.find(Company.class, id);
            if (dbCompany == null) {
                throw new RequestException("Could not company to approve");
            }
            dbCompany.setStatus(REGISTRATION_STATUS.APPROVED);
            if(dbCompany.isSupplier()) {
                // if new company create workspace
                GeoserverUtils.getGeoserverPublisher().createWorkspace(dbCompany.getId() + "");
                // update the keyphrases
                Query query = em.createNativeQuery("UPDATE company SET tsv = " + DBHelper.getCompanyTSV(dbCompany) +
                        ", tsvname = " + DBHelper.getCompanyNameTSV(dbCompany) + " where id = " + dbCompany.getId() +
                        ";");
                query.executeUpdate();
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if(em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RequestException("Server error");
        } finally {
            em.close();
        }
    }

    @Override
    public void removeCompany(Long id) throws RequestException {
        String userName = UserUtils.verifyUserAdmin(request);
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            Company company = em.find(Company.class, id);
            if(company == null) {
                throw new Exception("Company with ID " + id + " does not exist");
            }
            em.remove(company);
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if(em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RequestException("Error removing company, reason is " + e.getMessage());
        } finally {
            em.close();
        }
    }

    @Override
    public List<NewsItem> listNewsItems(int start, int limit, String orderby, String filter) throws RequestException {
        String userName = UserUtils.verifyUserAdmin(request);
        EntityManager em = EMF.get().createEntityManager();
        boolean hasFilter = filter != null && filter.length() > 0;
        // force orderby if null
        if(orderby == null) {
            orderby = "creationDate";
        }
        switch(orderby) {
            case "creationDate":
                orderby = "n.creationDate";
                break;
            default:
                orderby = "n.creationDate";
        }
        TypedQuery<NewsItem> query = em.createQuery("select n from NewsItem n" +
                (hasFilter ?  "  where UPPER(n.title) LIKE UPPER(:filter)" : "") +
                " order by " + orderby, NewsItem.class);
        if(hasFilter) {
            query.setParameter("filter", "%" + filter + "%");
        }
        query.setFirstResult(start);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public NewsItem getNewsItem(Long id) throws RequestException {
        String userName = UserUtils.verifyUserAdmin(request);
        EntityManager em = EMF.get().createEntityManager();
        return em.find(NewsItem.class, id);
    }

    @Override
    public void saveNewsItem(NewsItem newsItem) throws RequestException {
        String userName = UserUtils.verifyUserAdmin(request);
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            if(newsItem.getId() == null) {
                newsItem.setCreationDate(new Date());
                em.persist(newsItem);
            } else {
                NewsItem dbNewsItem = em.find(NewsItem.class, newsItem.getId());
                if(dbNewsItem == null) {
                    throw new RequestException("Could not find news item to update");
                }
                dbNewsItem.setTitle(newsItem.getTitle());
                dbNewsItem.setDescription(newsItem.getDescription());
                dbNewsItem.setWebsiteUrl(newsItem.getWebsiteUrl());
                em.merge(dbNewsItem);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if(em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RequestException("Server error");
        } finally {
            em.close();
        }
    }

    private DatasetProviderDTO createDatasetProviderDTO(DatasetProvider datasetProvider) {
        DatasetProviderDTO datasetProviderDTO = new DatasetProviderDTO();
        datasetProviderDTO.setId(datasetProvider.getId());
        datasetProviderDTO.setName(datasetProvider.getName());
        datasetProviderDTO.setIconURL(datasetProvider.getIconUrl());
        datasetProviderDTO.setUri(datasetProvider.getUri());
        datasetProviderDTO.setExtent(datasetProvider.getExtent());
        datasetProviderDTO.setCompanyDTO(CompanyHelper.createCompanyDTO(datasetProvider.getCompany()));
        return datasetProviderDTO;
    }

    @Override
    public List<DatasetProviderDTO> listDatasets() throws RequestException {
        String userName = UserUtils.verifyUserAdmin(request);
        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, userName);
            TypedQuery<DatasetProvider> query = em.createQuery("select d from DatasetProvider d where d.company = :company", DatasetProvider.class);
            query.setParameter("company", user.getCompany());
            return ListUtil.mutate(query.getResultList(), new ListUtil.Mutate<DatasetProvider, DatasetProviderDTO>() {
                @Override
                public DatasetProviderDTO mutate(DatasetProvider datasetProvider) {
                    return createDatasetProviderDTO(datasetProvider);
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RequestException("Error loading notifications");
        } finally {
            em.close();
        }
    }

    @Override
    public List<NotificationDTO> getNotifications() throws RequestException {
        String userName = UserUtils.verifyUserAdmin(request);
        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, userName);
            TypedQuery<AdminNotification> query = em.createQuery("select n from AdminNotification n ORDER BY n.creationDate DESC", AdminNotification.class);
            query.setMaxResults(10);
            return ListUtil.mutate(query.getResultList(), new ListUtil.Mutate<AdminNotification, NotificationDTO>() {
                @Override
                public NotificationDTO mutate(AdminNotification adminNotification) {
                    NotificationDTO notificationDTO = new NotificationDTO();
                    notificationDTO.setType(adminNotification.getType());
                    notificationDTO.setMessage(adminNotification.getMessage());
                    notificationDTO.setLinkId(adminNotification.getLinkId());
                    notificationDTO.setCreationDate(adminNotification.getCreationDate());
                    return notificationDTO;
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RequestException("Error loading notifications");
        } finally {
            em.close();
        }
    }

    @Override
    public FeedbackDTO getFeedback(String id) throws RequestException {
        String userName = UserUtils.verifyUserAdmin(request);
        if(id == null) {
            throw new RequestException("Feedback id cannot be null");
        }
        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, userName);
            Feedback feedback = em.find(Feedback.class, id);
            if(feedback == null) {
                throw new RequestException("No feedback with this id");
            }
            FeedbackDTO feedbackDTO = createFeedbackDTO(feedback);
            feedbackDTO.setMessages(ListUtil.mutate(feedback.getMessages(), new ListUtil.Mutate<Message, MessageDTO>() {
                @Override
                public MessageDTO mutate(Message message) {
                    return convertMessageToDTO(message);
                }
            }));
            return feedbackDTO;
        } catch (Exception e) {
            if(em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error(e.getMessage(), e);
            throw new RequestException(e instanceof RequestException ? e.getMessage() : "Could not load feedback, server error");
        } finally {
            em.close();
        }
    }

    @Override
    public MessageDTO addFeedbackMessage(String id, String text) throws RequestException {
        String userName = UserUtils.verifyUserAdmin(request);
        if(id == null) {
            throw new RequestException("Feedback id cannot be null");
        }
        if(text == null || text.length() == 0) {
            throw new RequestException("No message provided");
        }
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            User user = em.find(User.class, userName);
            Feedback feedback = em.find(Feedback.class, id);
            if(feedback == null) {
                throw new RequestException("No feedback with this id");
            }
            Message message = new Message();
            message.setFrom(user);
            message.setMessage(text);
            message.setCreationDate(new Date());
            em.persist(message);
            feedback.getMessages().add(message);
            em.getTransaction().commit();
            return convertMessageToDTO(message);
        } catch (Exception e) {
            if(em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error(e.getMessage(), e);
            throw new RequestException(e instanceof RequestException ? e.getMessage() : "Could not add message to request, server error");
        } finally {
            em.close();
        }
    }

    @Override
    public List<FeedbackDTO> listFeedbacks(String name) throws RequestException {
        String userName = UserUtils.verifyUserAdmin(request);
        EntityManager em = EMF.get().createEntityManager();
        try {
            TypedQuery<Feedback> query = em.createQuery("select f from Feedback f " +
                            (name != null ? "where f.customer.username = :userName" : "") +
                            " order by f.creationDate desc", Feedback.class);
            if(name != null) {
                query.setParameter("userName", name);
            }
            query.setFirstResult(0);
            query.setMaxResults(10);
            return ListUtil.mutate(query.getResultList(), new ListUtil.Mutate<Feedback, FeedbackDTO>() {
                @Override
                public FeedbackDTO mutate(Feedback feedback) {
                    return createFeedbackDTO(feedback);
                }
            });
        } catch (Exception e) {
            if(em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error(e.getMessage(), e);
            throw new RequestException(e instanceof RequestException ? e.getMessage() : "Could not load the feedbacks, server error");
        } finally {
            em.close();
        }
    }

    @Override
    public List<UserDescriptionDTO> listUsers(int start, int limit, String orderby, String filter) throws RequestException {
        String userName = UserUtils.verifyUserAdmin(request);
        EntityManager em = EMF.get().createEntityManager();
        try {
            boolean hasFilter = filter != null && filter.length() > 0;
            // force orderby if null
            if(orderby == null) {
                orderby = "creationDate";
            }
            switch(orderby) {
                case "creationDate":
                    orderby = "u.lastLoggedIn";
                    break;
                default:
                    orderby = "u.username";
            }
            TypedQuery<User> query = em.createQuery("select u from users u" +
                    (hasFilter ?  "  where UPPER(u.username) LIKE UPPER(:filter)" : "") +
                    " order by " + orderby + " DESC", User.class);
            if(hasFilter) {
                query.setParameter("filter", "%" + filter + "%");
            }
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return ListUtil.mutate(query.getResultList(), new ListUtil.Mutate<User, UserDescriptionDTO>() {
                @Override
                public UserDescriptionDTO mutate(User user) {
                    return createUserDescriptionDTO(user);
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RequestException("Server error");
        } finally {
            em.close();
        }
    }

    @Override
    public UserDescriptionDTO getUser(String userName) throws RequestException {
        UserUtils.verifyUserAdmin(request);
        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, userName);
            if(user == null) {
                throw new RequestException("User '" + userName + "' does not exist");
            }
            return createUserDescriptionDTO(user);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RequestException("Server error");
        } finally {
            em.close();
        }
    }

    private UserDescriptionDTO createUserDescriptionDTO(User user) {
        UserDescriptionDTO userDescriptionDTO = new UserDescriptionDTO();
        userDescriptionDTO.setName(user.getUsername());
        userDescriptionDTO.setEmail(user.getEmail());
        userDescriptionDTO.setUserRole(user.getRole());
        userDescriptionDTO.setCompanyDTO(CompanyHelper.createCompanyDTO(user.getCompany()));
        userDescriptionDTO.setStatus(user.getStatus());
        return userDescriptionDTO;
    }

    @Override
    public void saveUser(UserDescriptionDTO userDescriptionDTO) throws RequestException {
        UserUtils.verifyUserAdmin(request);
        if(userDescriptionDTO == null) {
            throw new RequestException("User description cannot be empty");
        }
        EntityManager em = EMF.get().createEntityManager();
        try {
            // update user
            em.getTransaction().begin();
            String userName = userDescriptionDTO.getName();
            User dbUser = em.find(User.class, userName);
            if(dbUser == null) {
                throw new RequestException("User does not exist");
            }
            Company dbCompany = null;
            if(userDescriptionDTO.getCompanyDTO() != null) {
                dbCompany = em.find(Company.class, userDescriptionDTO.getCompanyDTO().getId());
            }
            dbUser.setCompany(dbCompany);
            REGISTRATION_STATUS previousStatus = dbUser.getStatus();
            dbUser.setStatus(userDescriptionDTO.getStatus());
            dbUser.setRole(userDescriptionDTO.getUserRole());
            dbUser.setEmail(userDescriptionDTO.getEmail());
            String password = userDescriptionDTO.getPassword();
            if(password != null && password.length() > 0) {
                logger.trace("Changed password for user " + userName);
                dbUser.setPassword(com.geocento.webapps.eobroker.common.server.Utils.UserUtils.createPasswordHash(password));
            }
            em.getTransaction().commit();
            boolean statusChanged = previousStatus != userDescriptionDTO.getStatus();
            if(statusChanged) {
                // send email
                try {
                    // check for errors to report
                    MailContent mailContent = new MailContent(MailContent.EMAIL_TYPE.CONSUMER);
                    mailContent.addTitle("Your user account");
                    if(dbUser.getStatus() == REGISTRATION_STATUS.APPROVED) {
                        mailContent.addLine("Your EO Broker user account has been approved. You can now access the EO Broker.");
                    } else if(dbUser.getStatus() == REGISTRATION_STATUS.UNAPPROVED) {
                        mailContent.addLine("Your EO Broker user account has been disabled. Please contact us for more information.");
                    }
                    mailContent.sendEmail(dbUser.getEmail(), "Your EO Broker user account", false);
                } catch (Exception e) {
                    throw new RequestException("Could not send email, please retry");
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if(em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RequestException("Server error");
        } finally {
            em.close();
        }
    }

    @Override
    public void createUser(UserDescriptionDTO userDescriptionDTO) throws RequestException {
        UserUtils.verifyUserAdmin(request);
        if(userDescriptionDTO == null) {
            throw new RequestException("User description cannot be empty");
        }
        EntityManager em = EMF.get().createEntityManager();
        try {
            // update user
            em.getTransaction().begin();
            String userName = userDescriptionDTO.getName();
            User dbUser = em.find(User.class, userName);
            if(dbUser != null) {
                throw new RequestException("User already exists");
            }
            // create new user
            dbUser = new User();
            dbUser.setUsername(userDescriptionDTO.getName());
            dbUser.setCreationDate(new Date());
            // if password not provided generate random password
            String password = userDescriptionDTO.getPassword();
            if(password == null) {
                password = new KeyGenerator(8).CreateKey();
            }
            dbUser.setPassword(com.geocento.webapps.eobroker.common.server.Utils.UserUtils.createPasswordHash(password));
            dbUser.setFullName(userDescriptionDTO.getName());
            dbUser.setStatus(REGISTRATION_STATUS.APPROVED);
            em.persist(dbUser);
            Company dbCompany = null;
            if(userDescriptionDTO.getCompanyDTO() != null) {
                dbCompany = em.find(Company.class, userDescriptionDTO.getCompanyDTO().getId());
            }
            dbUser.setCompany(dbCompany);
            dbUser.setRole(userDescriptionDTO.getUserRole());
            dbUser.setEmail(userDescriptionDTO.getEmail());
            em.getTransaction().commit();
            // send email
            try {
                // check for errors to report
                MailContent mailContent = new MailContent(MailContent.EMAIL_TYPE.CONSUMER);
                mailContent.addTitle("Your user account");
                mailContent.addLine("A new EO Broker user account was created for you:");
                mailContent.addLine("<b>User name: </b>" + userName);
                mailContent.addLine("<b>Password: </b>" + password);
                mailContent.addLine("<b>Organisation: </b>" + dbUser.getCompany().getName());
                mailContent.sendEmail(dbUser.getEmail(), "User account creation", false);
            } catch (Exception e) {
                throw new RequestException("Could not send email, please retry");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if(em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RequestException("Server error");
        } finally {
            em.close();
        }
    }

    @Override
    public void removeUser(String userName) throws RequestException {
        UserUtils.verifyUserAdmin(request);
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            User user = em.find(User.class, userName);
            if(user == null) {
                throw new Exception("User with user name " + userName + " does not exist");
            }
            em.remove(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if(em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RequestException("Error removing user, reason is " + e.getMessage());
        } finally {
            em.close();
        }
    }

    @Override
    public ApplicationSettings getSettings() throws RequestException {
        return ServerUtil.getSettings();
    }

    @Override
    public void saveSettings(ApplicationSettings settings) throws RequestException {
        String userName = UserUtils.verifyUserAdmin(request);
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(settings);
            em.getTransaction().commit();
            logger.info("User " + userName + " has modified the application settings");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RequestException("Server error");
        } finally {
            em.close();
        }
    }

    @Override
    public LogsDTO getLogs() throws RequestException {
        FileAppender appender = (FileAppender) Logger.getRootLogger().getAppender("file");
        File file = new File(appender.getFile());
        try {
            LogsDTO logsDTO = new LogsDTO();
            logsDTO.setLogValue(FileUtils.readFileToString(file));
            return logsDTO;
        } catch (IOException e) {
            throw new RequestException("Server error");
        }
    }

    private FeedbackDTO createFeedbackDTO(Feedback feedback) {
        FeedbackDTO feedbackDTO = new FeedbackDTO();
        feedbackDTO.setId(feedback.getId());
        feedbackDTO.setUserDTO(createUserDTO(feedback.getCustomer()));
        feedbackDTO.setTopic(feedback.getTopic());
        feedbackDTO.setCreationDate(feedback.getCreationDate());
        return feedbackDTO;
    }

    private MessageDTO convertMessageToDTO(Message message) {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setId(message.getId());
        messageDTO.setFrom(message.getFrom().getUsername());
        messageDTO.setMessage(message.getMessage());
        messageDTO.setCreationDate(message.getCreationDate());
        return messageDTO;
    }

    static public UserDTO createUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setName(user.getUsername());
        userDTO.setCompanyDTO(CompanyHelper.createCompanyDTO(user.getCompany()));
        return userDTO;
    }

    @Override
    public List<CompanyDTO> findCompanies(String textFilter) {
/*
        // check if last character is a space
        boolean partialMatch = !text.endsWith(" ");
        text.trim();
        if(text.length() == 0) {
            return null;
        }
        // break down text into sub words
        String[] words = text.split(" ");
        String keywords = StringUtils.join(words, " | ");
        if(partialMatch) {
            keywords += ":*";
        }
*/
        String keywords = DBHelper.generateKeywords(textFilter);
        // change the last word so that it allows for partial match
        String sqlStatement = "SELECT id, \"name\", iconurl, ts_rank(tsvname, keywords, 8) AS rank, id\n" +
                "          FROM company, to_tsquery('" + keywords + "') AS keywords\n" +
                "          WHERE tsvname @@ keywords\n" +
                "          ORDER BY rank\n" +
                "          LIMIT 10;";
        EntityManager em = EMF.get().createEntityManager();
        Query q = em.createNativeQuery(sqlStatement);
        List<Object[]> results = q.getResultList();
        List<CompanyDTO> suggestions = new ArrayList<CompanyDTO>();
        for(Object[] result : results) {
            CompanyDTO companyDTO = new CompanyDTO();
            companyDTO.setId((Long) result[0]);
            companyDTO.setName((String) result[1]);
            companyDTO.setIconURL((String) result[2]);
            suggestions.add(companyDTO);
        }
        return suggestions;
    }

}
