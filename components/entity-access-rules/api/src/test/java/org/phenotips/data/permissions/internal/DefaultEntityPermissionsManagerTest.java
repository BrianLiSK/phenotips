/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 */
package org.phenotips.data.permissions.internal;

import org.phenotips.data.permissions.AccessLevel;
import org.phenotips.data.permissions.EntityAccess;
import org.phenotips.data.permissions.EntityPermissionsManager;
import org.phenotips.data.permissions.PermissionsConfiguration;
import org.phenotips.data.permissions.Visibility;
import org.phenotips.data.permissions.internal.access.EditAccessLevel;
import org.phenotips.data.permissions.internal.access.ManageAccessLevel;
import org.phenotips.data.permissions.internal.access.NoAccessLevel;
import org.phenotips.data.permissions.internal.access.OwnerAccessLevel;
import org.phenotips.data.permissions.internal.access.ViewAccessLevel;
import org.phenotips.data.permissions.internal.visibility.HiddenVisibility;
import org.phenotips.data.permissions.internal.visibility.MockVisibility;
import org.phenotips.data.permissions.internal.visibility.PrivateVisibility;
import org.phenotips.data.permissions.internal.visibility.PublicVisibility;
import org.phenotips.entities.PrimaryEntity;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the default {@link EntityPermissionsManager} implementation, {@link DefaultEntityPermissionsManager}.
 *
 * @version $Id$
 */
public class DefaultEntityPermissionsManagerTest
{
    @Rule
    public final MockitoComponentMockingRule<EntityPermissionsManager> mocker =
        new MockitoComponentMockingRule<EntityPermissionsManager>(DefaultEntityPermissionsManager.class);

    private AccessLevel none = new NoAccessLevel();

    private AccessLevel view = new ViewAccessLevel();

    private AccessLevel edit = new EditAccessLevel();

    private AccessLevel manage = new ManageAccessLevel();

    private AccessLevel owner = new OwnerAccessLevel();

    private Visibility privateVisibility = new MockVisibility("private", 0, this.none);

    private Visibility publicVisibility = new MockVisibility("public", 50, this.view);

    private Visibility disabledOpenVisibility = new MockVisibility("open", 80, this.edit, true);

    /** Basic tests for {@link EntityPermissionsManager#listAccessLevels()}. */
    @Test
    public void listAccessLevels() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        List<AccessLevel> levels = new ArrayList<>();
        levels.add(this.edit);
        levels.add(this.none);
        levels.add(this.owner);
        levels.add(this.view);
        levels.add(this.manage);
        when(cm.<AccessLevel>getInstanceList(AccessLevel.class)).thenReturn(levels);
        Collection<AccessLevel> returnedLevels = this.mocker.getComponentUnderTest().listAccessLevels();
        Assert.assertEquals(3, returnedLevels.size());
        Iterator<AccessLevel> it = returnedLevels.iterator();
        Assert.assertSame(this.view, it.next());
        Assert.assertSame(this.edit, it.next());
        Assert.assertSame(this.manage, it.next());
        Assert.assertFalse(returnedLevels.contains(this.none));
        Assert.assertFalse(returnedLevels.contains(this.owner));
    }

    /** {@link EntityPermissionsManager#listAccessLevels()} returns an empty list when no implementations available. */
    @Test
    public void listAccessLevelsWithNoComponents() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        when(cm.<AccessLevel>getInstanceList(AccessLevel.class)).thenReturn(Collections.<AccessLevel>emptyList());
        Collection<AccessLevel> returnedLevels = this.mocker.getComponentUnderTest().listAccessLevels();
        Assert.assertTrue(returnedLevels.isEmpty());
    }

    /** {@link EntityPermissionsManager#listAccessLevels()} returns an empty list when looking up components fails. */
    @Test
    public void listAccessLevelsWithLookupExceptions() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        when(cm.<AccessLevel>getInstanceList(AccessLevel.class)).thenThrow(new ComponentLookupException("None"));
        Collection<AccessLevel> returnedLevels = this.mocker.getComponentUnderTest().listAccessLevels();
        Assert.assertTrue(returnedLevels.isEmpty());
    }

    /** {@link EntityPermissionsManager#resolveAccessLevel(String)} returns the right implementation. */
    @Test
    public void resolveAccessLevel() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        AccessLevel edit = mock(AccessLevel.class);
        when(cm.getInstance(AccessLevel.class, "edit")).thenReturn(edit);
        Assert.assertSame(edit, this.mocker.getComponentUnderTest().resolveAccessLevel("edit"));
    }

    /** {@link EntityPermissionsManager#resolveAccessLevel(String)} returns null if an unknown level is requested. */
    @Test
    public void resolveAccessLevelWithUnknownAccess() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        when(cm.getInstance(AccessLevel.class, "unknown")).thenThrow(new ComponentLookupException("No such component"));
        Assert.assertNull(this.mocker.getComponentUnderTest().resolveAccessLevel("unknown"));
    }

    /** {@link EntityPermissionsManager#resolveAccessLevel(String)} returns null if a null or blank level is requested. */
    @Test
    public void resolveAccessLevelWithNoAccess() throws ComponentLookupException
    {
        Assert.assertNull(this.mocker.getComponentUnderTest().resolveAccessLevel(null));
        Assert.assertNull(this.mocker.getComponentUnderTest().resolveAccessLevel(""));
        Assert.assertNull(this.mocker.getComponentUnderTest().resolveAccessLevel(" "));
    }

    /** Basic test for {@link EntityPermissionsManager#listVisibilityOptions()}. */
    @Test
    public void listVisibilityOptionsSkipsDisabledVisibilitiesAndReordersByPriority() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        List<Visibility> visibilities = new ArrayList<>();
        visibilities.add(this.publicVisibility);
        visibilities.add(this.privateVisibility);
        visibilities.add(this.disabledOpenVisibility);
        when(cm.<Visibility>getInstanceList(Visibility.class)).thenReturn(visibilities);
        Collection<Visibility> returnedVisibilities = this.mocker.getComponentUnderTest().listVisibilityOptions();
        Assert.assertEquals(2, returnedVisibilities.size());
        Iterator<Visibility> it = returnedVisibilities.iterator();
        Assert.assertSame(this.privateVisibility, it.next());
        Assert.assertSame(this.publicVisibility, it.next());
    }

    /** Basic test for {@link EntityPermissionsManager#listAllVisibilityOptions()}. */
    @Test
    public void listAllVisibilityOptionsReordersByPriority() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        List<Visibility> visibilities = new ArrayList<>();
        visibilities.add(this.publicVisibility);
        visibilities.add(this.privateVisibility);
        visibilities.add(this.disabledOpenVisibility);
        when(cm.<Visibility>getInstanceList(Visibility.class)).thenReturn(visibilities);
        Collection<Visibility> returnedVisibilities = this.mocker.getComponentUnderTest().listAllVisibilityOptions();
        Assert.assertEquals(3, returnedVisibilities.size());
        Iterator<Visibility> it = returnedVisibilities.iterator();
        Assert.assertSame(this.privateVisibility, it.next());
        Assert.assertSame(this.publicVisibility, it.next());
        Assert.assertSame(this.disabledOpenVisibility, it.next());
    }

    /** {@link EntityPermissionsManager#listVisibilityOptions()} returns an empty list when no implementations available. */
    @Test
    public void listVisibilityOptionsWithNoComponentsEmptyList() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        when(cm.<Visibility>getInstanceList(Visibility.class)).thenReturn(Collections.<Visibility>emptyList());
        Collection<Visibility> returnedVisibilities = this.mocker.getComponentUnderTest().listVisibilityOptions();
        Assert.assertTrue(returnedVisibilities.isEmpty());
    }

    /**
     * {@link EntityPermissionsManager#listAllVisibilityOptions()} returns an empty list when no implementations available.
     */
    @Test
    public void listAllVisibilityOptionsWithNoComponentsReturnsEmptyList() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        when(cm.<Visibility>getInstanceList(Visibility.class)).thenReturn(Collections.<Visibility>emptyList());
        Collection<Visibility> returnedVisibilities = this.mocker.getComponentUnderTest().listAllVisibilityOptions();
        Assert.assertTrue(returnedVisibilities.isEmpty());
    }

    /** {@link EntityPermissionsManager#listVisibilityOptions()} returns an empty list when all visibilities are disabled. */
    @Test
    public void listVisibilityOptionsWithOnlyDisabledVisibilitiesReturnsEmptyList() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        List<Visibility> visibilities = new ArrayList<>();
        visibilities.add(this.disabledOpenVisibility);
        when(cm.<Visibility>getInstanceList(Visibility.class)).thenReturn(visibilities);
        Collection<Visibility> returnedVisibilities = this.mocker.getComponentUnderTest().listVisibilityOptions();
        Assert.assertTrue(returnedVisibilities.isEmpty());
    }

    /** {@link EntityPermissionsManager#listVisibilityOptions()} returns an empty list when looking up components fails. */
    @Test
    public void listVisibilityOptionsWithLookupExceptions() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        when(cm.<Visibility>getInstanceList(Visibility.class)).thenThrow(new ComponentLookupException("None"));
        Collection<Visibility> returnedVisibilities = this.mocker.getComponentUnderTest().listVisibilityOptions();
        Assert.assertTrue(returnedVisibilities.isEmpty());
    }

    @Test
    public void getDefaultVisibilityForwardsCalls() throws ComponentLookupException
    {
        PermissionsConfiguration config = this.mocker.getInstance(PermissionsConfiguration.class);
        when(config.getDefaultVisibility()).thenReturn("public");
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        when(cm.getInstance(Visibility.class, "public")).thenReturn(this.publicVisibility);
        Assert.assertSame(this.publicVisibility, this.mocker.getComponentUnderTest().getDefaultVisibility());
    }

    /** {@link EntityPermissionsManager#resolveVisibility(String)} returns the right implementation. */
    @Test
    public void resolveVisibility() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        when(cm.getInstance(Visibility.class, "public")).thenReturn(this.publicVisibility);
        Assert.assertSame(this.publicVisibility, this.mocker.getComponentUnderTest().resolveVisibility("public"));
    }

    /** {@link EntityPermissionsManager#resolveVisibility(String)} returns null if a null or blank visibility is requested. */
    @Test
    public void resolveVisibilityWithNoAccess() throws ComponentLookupException
    {
        Assert.assertNull(this.mocker.getComponentUnderTest().resolveVisibility(null));
        Assert.assertNull(this.mocker.getComponentUnderTest().resolveVisibility(""));
        Assert.assertNull(this.mocker.getComponentUnderTest().resolveVisibility(" "));
    }

    /** {@link EntityPermissionsManager#resolveVisibility(String)} returns null if an unknown visibility is requested. */
    @Test
    public void resolveVisibilityWithUnknownVisibilityTest() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        when(cm.getInstance(Visibility.class, "unknown")).thenThrow(new ComponentLookupException("No such component"));
        Assert.assertNull(this.mocker.getComponentUnderTest().resolveVisibility("unknown"));
    }

    /** {@link EntityPermissionsManager#getEntityAccess(PrimaryEntity)} returns a {@link DefaultEntityAccess}. */
    @Test
    public void getPrimaryEntityAccess() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        PrimaryEntity entity = mock(PrimaryEntity.class);
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        when(cm.getInstance(EntityAccessHelper.class)).thenReturn(helper);
        EntityAccess result = this.mocker.getComponentUnderTest().getEntityAccess(entity);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof DefaultEntityAccess);
    }

    /** {@link EntityPermissionsManager#getEntityAccess(PrimaryEntity)} returns a {@link DefaultEntityAccess}. */
    @Test
    public void getPrimaryEntityAccessWithMissingHelper() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        PrimaryEntity entity = mock(PrimaryEntity.class);
        when(cm.getInstance(EntityAccessHelper.class)).thenThrow(new ComponentLookupException("Missing"));
        EntityAccess result = this.mocker.getComponentUnderTest().getEntityAccess(entity);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof DefaultEntityAccess);
    }

    @Test
    public void filterCollectionByVisibilityWithEmptyInputReturnsEmptyCollection() throws ComponentLookupException
    {
        Collection<? extends PrimaryEntity> result = this.mocker.getComponentUnderTest()
            .filterByVisibility(Collections.<PrimaryEntity>emptyList(), new PrivateVisibility());
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void filterIteratorByVisibilityWithEmptyInputReturnsEmptyIterator() throws ComponentLookupException
    {
        Iterator<? extends PrimaryEntity> result = this.mocker.getComponentUnderTest()
            .filterByVisibility(Collections.<PrimaryEntity>emptyIterator(), new PrivateVisibility());
        Assert.assertNotNull(result);
        Assert.assertFalse(result.hasNext());
    }

    @Test
    public void filterCollectionByVisibilityWithNullInputReturnsEmptyCollection() throws ComponentLookupException
    {
        Collection<? extends PrimaryEntity> result = this.mocker.getComponentUnderTest()
            .filterByVisibility((Collection<PrimaryEntity>) null, new PrivateVisibility());
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void filterIteratorByVisibilityWithNullInputReturnsEmptyIterator() throws ComponentLookupException
    {
        Iterator<? extends PrimaryEntity> result = this.mocker.getComponentUnderTest()
            .filterByVisibility((Iterator<PrimaryEntity>) null, new PrivateVisibility());
        Assert.assertNotNull(result);
        Assert.assertFalse(result.hasNext());
    }

    @Test
    public void filterCollectionByVisibilityWithValidInputFiltersPrimaryEntitys() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        when(cm.getInstance(EntityAccessHelper.class)).thenReturn(helper);

        Collection<PrimaryEntity> input = new ArrayList<>();
        PrimaryEntity p1 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p1)).thenReturn(new PublicVisibility());
        input.add(p1);
        PrimaryEntity p2 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p2)).thenReturn(new HiddenVisibility());
        input.add(p2);
        PrimaryEntity p3 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p3)).thenReturn(new PrivateVisibility());
        input.add(p3);

        Collection<? extends PrimaryEntity> result = this.mocker.getComponentUnderTest()
            .filterByVisibility(input, new PrivateVisibility());
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Iterator<? extends PrimaryEntity> it = result.iterator();
        Assert.assertSame(p1, it.next());
        Assert.assertSame(p3, it.next());
    }

    @Test
    public void filterIteratorByVisibilityWithValidInputFiltersPrimaryEntitys() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        when(cm.getInstance(EntityAccessHelper.class)).thenReturn(helper);

        Collection<PrimaryEntity> input = new ArrayList<>();
        PrimaryEntity p1 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p1)).thenReturn(new PublicVisibility());
        input.add(p1);
        PrimaryEntity p2 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p2)).thenReturn(new HiddenVisibility());
        input.add(p2);
        PrimaryEntity p3 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p3)).thenReturn(new PrivateVisibility());
        input.add(p3);

        Iterator<? extends PrimaryEntity> result = this.mocker.getComponentUnderTest()
            .filterByVisibility(input.iterator(), new PrivateVisibility());
        Assert.assertNotNull(result);
        Assert.assertTrue(result.hasNext());
        Assert.assertSame(p1, result.next());
        Assert.assertTrue(result.hasNext());
        Assert.assertSame(p3, result.next());
        Assert.assertFalse(result.hasNext());
    }

    @Test
    public void filterCollectionByVisibilityWithNullInInputFiltersValidPrimaryEntitys() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        when(cm.getInstance(EntityAccessHelper.class)).thenReturn(helper);

        Collection<PrimaryEntity> input = new ArrayList<>();
        PrimaryEntity p1 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p1)).thenReturn(new PublicVisibility());
        input.add(p1);
        PrimaryEntity p2 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p2)).thenReturn(new HiddenVisibility());
        input.add(p2);
        input.add(null);
        PrimaryEntity p3 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p3)).thenReturn(new PrivateVisibility());
        input.add(p3);
        input.add(null);

        Collection<? extends PrimaryEntity> result = this.mocker.getComponentUnderTest()
            .filterByVisibility(input, new PrivateVisibility());
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Iterator<? extends PrimaryEntity> it = result.iterator();
        Assert.assertSame(p1, it.next());
        Assert.assertSame(p3, it.next());
    }

    @Test
    public void filterIteratorByVisibilityWithNullInInputFiltersValidPrimaryEntitys() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        when(cm.getInstance(EntityAccessHelper.class)).thenReturn(helper);

        Collection<PrimaryEntity> input = new ArrayList<>();
        PrimaryEntity p1 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p1)).thenReturn(new PublicVisibility());
        input.add(p1);
        PrimaryEntity p2 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p2)).thenReturn(new HiddenVisibility());
        input.add(p2);
        input.add(null);
        PrimaryEntity p3 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p3)).thenReturn(new PrivateVisibility());
        input.add(p3);
        input.add(null);

        Iterator<? extends PrimaryEntity> result = this.mocker.getComponentUnderTest()
            .filterByVisibility(input.iterator(), new PrivateVisibility());
        Assert.assertNotNull(result);
        Assert.assertTrue(result.hasNext());
        Assert.assertSame(p1, result.next());
        Assert.assertTrue(result.hasNext());
        Assert.assertSame(p3, result.next());
        Assert.assertFalse(result.hasNext());
    }

    @Test
    public void filterCollectionByVisibilityWithNonMatchingInputReturnsEmptyList() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        when(cm.getInstance(EntityAccessHelper.class)).thenReturn(helper);

        Collection<PrimaryEntity> input = new ArrayList<>();
        PrimaryEntity p1 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p1)).thenReturn(new HiddenVisibility());
        input.add(p1);
        PrimaryEntity p2 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p2)).thenReturn(new HiddenVisibility());
        input.add(p2);
        input.add(null);
        PrimaryEntity p3 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p3)).thenReturn(new PrivateVisibility());
        input.add(p3);

        Collection<? extends PrimaryEntity> result = this.mocker.getComponentUnderTest()
            .filterByVisibility(input, new PublicVisibility());
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void filterIteratorByVisibilityWithNonMatchingInputReturnsEmptyIterator() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        when(cm.getInstance(EntityAccessHelper.class)).thenReturn(helper);

        Collection<PrimaryEntity> input = new ArrayList<>();
        PrimaryEntity p1 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p1)).thenReturn(new HiddenVisibility());
        input.add(p1);
        PrimaryEntity p2 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p2)).thenReturn(new HiddenVisibility());
        input.add(p2);
        input.add(null);
        PrimaryEntity p3 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p3)).thenReturn(new PrivateVisibility());
        input.add(p3);

        Iterator<? extends PrimaryEntity> result = this.mocker.getComponentUnderTest()
            .filterByVisibility(input.iterator(), new PublicVisibility());
        Assert.assertNotNull(result);
        Assert.assertFalse(result.hasNext());
    }

    @Test
    public void filterCollectionByVisibilityWithNullThresholdReturnsUnfilteredList() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        when(cm.getInstance(EntityAccessHelper.class)).thenReturn(helper);

        Collection<PrimaryEntity> input = new ArrayList<>();
        PrimaryEntity p1 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p1)).thenReturn(new PublicVisibility());
        input.add(p1);
        PrimaryEntity p2 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p2)).thenReturn(new HiddenVisibility());
        input.add(p2);
        input.add(null);
        PrimaryEntity p3 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p3)).thenReturn(new PrivateVisibility());
        input.add(p3);

        Collection<? extends PrimaryEntity> result = this.mocker.getComponentUnderTest()
            .filterByVisibility(input, null);
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.size());
        Iterator<? extends PrimaryEntity> it = result.iterator();
        Assert.assertSame(p1, it.next());
        Assert.assertSame(p2, it.next());
        Assert.assertNull(it.next());
        Assert.assertSame(p3, it.next());
    }

    @Test
    public void filterIteratorByVisibilityWithNullThresholdReturnsUnfilteredIterator() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        when(cm.getInstance(EntityAccessHelper.class)).thenReturn(helper);

        Collection<PrimaryEntity> input = new ArrayList<>();
        PrimaryEntity p1 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p1)).thenReturn(new PublicVisibility());
        input.add(p1);
        PrimaryEntity p2 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p2)).thenReturn(new HiddenVisibility());
        input.add(p2);
        input.add(null);
        PrimaryEntity p3 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p3)).thenReturn(new PrivateVisibility());
        input.add(p3);

        Iterator<? extends PrimaryEntity> result = this.mocker.getComponentUnderTest()
            .filterByVisibility(input.iterator(), null);
        Assert.assertTrue(result.hasNext());
        Assert.assertSame(p1, result.next());
        Assert.assertTrue(result.hasNext());
        Assert.assertSame(p2, result.next());
        Assert.assertTrue(result.hasNext());
        Assert.assertNull(result.next());
        Assert.assertTrue(result.hasNext());
        Assert.assertSame(p3, result.next());
        Assert.assertFalse(result.hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void filterIteratorByVisibilityReturnsReadonlyIterator() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        when(cm.getInstance(EntityAccessHelper.class)).thenReturn(helper);

        Collection<PrimaryEntity> input = new ArrayList<>();
        PrimaryEntity p1 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p1)).thenReturn(new PublicVisibility());
        input.add(p1);
        PrimaryEntity p2 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p2)).thenReturn(new HiddenVisibility());
        input.add(p2);
        input.add(null);
        PrimaryEntity p3 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p3)).thenReturn(new PrivateVisibility());
        input.add(p3);

        Iterator<? extends PrimaryEntity> result = this.mocker.getComponentUnderTest()
            .filterByVisibility(input.iterator(), new PrivateVisibility());
        Assert.assertSame(p1, result.next());
        result.remove();
    }

    @Test(expected = NoSuchElementException.class)
    public void filterIteratorByVisibilityReturnsCorrectIterator() throws ComponentLookupException
    {
        ComponentManager cm = this.mocker.getInstance(ComponentManager.class, "context");
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        when(cm.getInstance(EntityAccessHelper.class)).thenReturn(helper);

        Collection<PrimaryEntity> input = new ArrayList<>();
        PrimaryEntity p1 = mock(PrimaryEntity.class);
        when(helper.getVisibility(p1)).thenReturn(new PublicVisibility());
        input.add(p1);

        Iterator<? extends PrimaryEntity> result = this.mocker.getComponentUnderTest()
            .filterByVisibility(input.iterator(), new PrivateVisibility());
        Assert.assertSame(p1, result.next());
        Assert.assertFalse(result.hasNext());
        result.next();
    }
}