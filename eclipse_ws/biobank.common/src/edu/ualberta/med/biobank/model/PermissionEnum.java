package edu.ualberta.med.biobank.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ualberta.med.biobank.common.util.NotAProxy;

/**
 * The id of these enumerations are saved in the database. Therefore, DO NOT
 * CHANGE THESE ENUM IDS (unless you are prepared to write an upgrade script).
 * However, order and enum name can be modified freely.
 * <p>
 * Also, these enums should probably never be deleted, unless they are not used
 * in <em>any</em> database. Instead, they should be deprecated and probably
 * always return false when checking allow-ability.
 * 
 * @author Jonathan Ferland
 * 
 */
public enum PermissionEnum implements NotAProxy, Serializable {
    SPECIMEN_CREATE(2),
    SPECIMEN_READ(3),
    SPECIMEN_UPDATE(4),
    SPECIMEN_DELETE(5),
    SPECIMEN_LINK(6),
    SPECIMEN_ASSIGN(7),

    SITE_CREATE(8),
    SITE_READ(9),
    SITE_UPDATE(10),
    SITE_DELETE(11),

    PATIENT_CREATE(12),
    PATIENT_READ(13),
    PATIENT_UPDATE(14),
    PATIENT_DELETE(15),
    PATIENT_MERGE(16),

    COLLECTION_EVENT_CREATE(17),
    COLLECTION_EVENT_READ(18),
    COLLECTION_EVENT_UPDATE(19),
    COLLECTION_EVENT_DELETE(20),

    PROCESSING_EVENT_CREATE(21),
    PROCESSING_EVENT_READ(22),
    PROCESSING_EVENT_UPDATE(23),
    PROCESSING_EVENT_DELETE(24),

    ORIGIN_INFO_CREATE(25),
    ORIGIN_INFO_READ(26),
    ORIGIN_INFO_UPDATE(27),
    ORIGIN_INFO_DELETE(28),

    DISPATCH_CREATE(29),
    DISPATCH_READ(30),
    DISPATCH_CHANGE_STATE(31),
    DISPATCH_UPDATE(32),
    DISPATCH_DELETE(33),

    RESEARCH_GROUP_CREATE(34),
    RESEARCH_GROUP_READ(35),
    RESEARCH_GROUP_UPDATE(36),
    RESEARCH_GROUP_DELETE(37),

    STUDY_CREATE(38),
    STUDY_READ(39),
    STUDY_UPDATE(40),
    STUDY_DELETE(41),

    REQUEST_CREATE(42),
    REQUEST_READ(43),
    REQUEST_UPDATE(44),
    REQUEST_DELETE(45),

    REQUEST_PROCESS(46),

    CLINIC_CREATE(47),
    CLINIC_READ(48),
    CLINIC_UPDATE(49),
    CLINIC_DELETE(50),

    CONTAINER_TYPE_CREATE(52),
    CONTAINER_TYPE_READ(53),
    CONTAINER_TYPE_UPDATE(54),
    CONTAINER_TYPE_DELETE(55),

    CONTAINER_CREATE(56),
    CONTAINER_READ(57),
    CONTAINER_UPDATE(58),
    CONTAINER_DELETE(59),

    SPECIMEN_TYPE_CREATE(60),
    SPECIMEN_TYPE_READ(61),
    SPECIMEN_TYPE_UPDATE(62),
    SPECIMEN_TYPE_DELETE(63),

    LOGGING(64),
    REPORTS(65),

    SPECIMEN_LIST(66),
    LABEL_PRINTING(67);

    private static final List<PermissionEnum> VALUES_LIST = Collections
        .unmodifiableList(Arrays.asList(values()));
    private static final Map<Integer, PermissionEnum> VALUES_MAP;

    static {
        Map<Integer, PermissionEnum> map =
            new HashMap<Integer, PermissionEnum>();

        for (PermissionEnum permissionEnum : values()) {
            PermissionEnum check = map.get(permissionEnum.getId());
            if (check != null) {
                throw new RuntimeException("permission enum value "
                    + permissionEnum.getId() + " used multiple times");
            }

            map.put(permissionEnum.getId(), permissionEnum);
        }

        VALUES_MAP = Collections.unmodifiableMap(map);
    }

    private final Integer id;

    private PermissionEnum(Integer permissionId) {
        this.id = permissionId;
    }

    public static List<PermissionEnum> valuesList() {
        return VALUES_LIST;
    }

    public static Map<Integer, PermissionEnum> valuesMap() {
        return VALUES_MAP;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name(); // TODO: localized name?
    }

    public static PermissionEnum fromId(Integer id) {
        return valuesMap().get(id);
    }

    /**
     * Whether the given {@link User} has this {@link PermissionEnum} on
     * <em>any</em> {@link Center} or {@link Study}.
     *
     * @see {@link #isMembershipAllowed(Membership, Center, Study)}
     * @param user
     * @return
     */
    public boolean isAllowed(User user) {
        return isAllowed(user, null, null);
    }

    /**
     * Whether the given {@link User} has this {@link PermissionEnum} on
     * <em>any</em> {@link Center}, but a specific {@link Study}.
     *
     * @see {@link #isAllowed(User)}
     * @param user
     * @return
     */
    public boolean isAllowed(User user, Study study) {
        return isAllowed(user, null, study);
    }

    /**
     * Whether the given {@link User} has this {@link PermissionEnum} on
     * <em>any</em> {@link Study}, but a specific {@link Center}.
     *
     * @see {@link #isAllowed(User)}
     * @param user
     * @return
     */
    public boolean isAllowed(User user, Center center) {
        return isAllowed(user, center, null);
    }

    /**
     *
     * @param user
     * @param center if null, {@link Center} does not matter.
     * @param study if null, {@link Study} does not matter.
     * @return
     */
    public boolean isAllowed(User user, Center center, Study study) {
        for (Membership m : user.getAllMemberships()) {
            if (isMembershipAllowed(m, center, study)) return true;
        }
        return false;
    }

    /**
     * This is a confusing check. If {@link Center} is null, it means we do not
     * care about its value. If {@link Membership#getCenter()} is null, we don't
     * care about the {@link Center} parameter's value. If neither is null, then
     * they must be equal, because we care about the {@link Center} paramter's
     * value, and it must match the {@link Membership#getCenter()} value. The
     * same applies to {@link Study}.
     * 
     * @param membership
     * @param center
     * @param study
     * @return
     */
    private boolean isMembershipAllowed(Membership membership, Center center,
        Study study) {
        boolean hasCenter = center == null || membership.getCenter() == null
            || membership.getCenter().equals(center);
        boolean hasStudy = study == null || membership.getStudy() == null
            || membership.getStudy().equals(study);
        boolean hasPermission = membership.getAllPermissions().contains(this);

        boolean isAllowed = hasCenter && hasStudy && hasPermission;
        return isAllowed;
    }
}
