package com.pgtr.dao;

import com.pgtr.model.NutritionPlanItem;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object for Cut and Bulk nutrition meal plans.
 */
public interface NutritionPlanDAO {
    boolean saveItem(NutritionPlanItem item) throws SQLException;
    boolean updateItem(NutritionPlanItem item) throws SQLException;
    boolean deleteItem(String id, String userId) throws SQLException;
    List<NutritionPlanItem> getItemsByMode(String userId, String planMode) throws SQLException;
    boolean setItemConsumed(String id, String userId, boolean consumed) throws SQLException;
    void resetToDefaultPlan(String userId, String planMode) throws SQLException;
}
