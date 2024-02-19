/*
 * Copyright (c) 2023 Proton AG
 *
 * This file is part of ProtonVPN.
 *
 * ProtonVPN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonVPN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonVPN.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.protonvpn.android.redesign.settings.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
import android.provider.Settings.EXTRA_APP_PACKAGE
import android.provider.Settings.EXTRA_CHANNEL_ID
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.protonvpn.android.BuildConfig
import com.protonvpn.android.R
import com.protonvpn.android.redesign.base.ui.LocalVpnUiDelegate
import com.protonvpn.android.redesign.base.ui.ProtonAlert
import com.protonvpn.android.redesign.settings.ui.nav.SubSettingsScreen
import com.protonvpn.android.ui.ProtocolSelectionActivity
import com.protonvpn.android.ui.account.AccountActivity
import com.protonvpn.android.ui.drawer.LogActivity
import com.protonvpn.android.ui.drawer.bugreport.DynamicReportActivity
import com.protonvpn.android.ui.planupgrade.UpgradeDialogActivity
import com.protonvpn.android.ui.planupgrade.UpgradeNetShieldHighlightsFragment
import com.protonvpn.android.ui.planupgrade.UpgradeSplitTunnelingHighlightsFragment
import com.protonvpn.android.ui.planupgrade.UpgradeVpnAcceleratorHighlightsFragment
import com.protonvpn.android.ui.settings.OssLicensesActivity
import com.protonvpn.android.ui.settings.SettingsAlwaysOnActivity
import com.protonvpn.android.ui.settings.SettingsTelemetryActivity
import com.protonvpn.android.utils.AndroidUtils
import com.protonvpn.android.utils.AndroidUtils.launchActivity
import com.protonvpn.android.utils.openUrl
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionNorm
import me.proton.core.compose.theme.captionWeak
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.defaultSmallNorm
import me.proton.core.compose.theme.defaultSmallStrongUnspecified
import me.proton.core.compose.theme.defaultSmallWeak
import me.proton.core.compose.theme.defaultStrongNorm
import me.proton.core.compose.theme.defaultWeak
import me.proton.core.presentation.R as CoreR


@SuppressLint("InlinedApi")
@Composable
fun SettingsRoute(
    signOut: () -> Unit,
    onNavigateToSubSetting: (SubSettingsScreen.Type) -> Unit
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val viewState = viewModel.viewState.collectAsStateWithLifecycle(initialValue = null).value

    if (viewState == null) {
        // Return a composable even when viewState == null to avoid transition glitches
        Box(modifier = Modifier.fillMaxSize()) {}
        return
    }

    val context = LocalContext.current
    val vpnUiDelegate = LocalVpnUiDelegate.current

    val protocolLauncher =
        rememberLauncherForActivityResult(contract = ProtocolSelectionActivity.createContract()) { protocol ->
            if (protocol != null)
                viewModel.updateProtocol(vpnUiDelegate, protocol)
        }
    SettingsView(
        viewState = viewState,
        settingsActions = remember {
            SettingsActions(
                onAccountClick = {
                    context.launchActivity<AccountActivity>()
                },
                signOut = signOut,
                onNetShieldClick = {
                    onNavigateToSubSetting(SubSettingsScreen.Type.NetShield)
                },
                onNetShieldUpgradeClick = {
                    UpgradeDialogActivity.launch<UpgradeNetShieldHighlightsFragment>(context)
                },
                onSplitTunnelClick = {
                    onNavigateToSubSetting(SubSettingsScreen.Type.SplitTunneling)
                },
                onSplitTunnelUpgrade = {
                    UpgradeDialogActivity.launch<UpgradeSplitTunnelingHighlightsFragment>(context)
                },
                onAlwaysOnClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        context.startActivity(Intent(context, SettingsAlwaysOnActivity::class.java))
                },
                onProtocolClick = {
                    protocolLauncher.launch(viewState.protocol.value)
                },
                onVpnAcceleratorClick = {
                    onNavigateToSubSetting(SubSettingsScreen.Type.VpnAccelerator)
                },
                onVpnAcceleratorUpgrade = {
                    UpgradeDialogActivity.launch<UpgradeVpnAcceleratorHighlightsFragment>(context)
                },
                onAdvancedSettingsClick = {
                    onNavigateToSubSetting(SubSettingsScreen.Type.Advanced)
                },
                onNotificationsClick = {
                    val intent = Intent(ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(EXTRA_APP_PACKAGE, context.packageName)
                        putExtra(EXTRA_CHANNEL_ID, context.applicationInfo.uid)
                    }

                    context.startActivity(intent)
                },
                onOnHelpCenterClick = {
                    context.openUrl(context.getString(R.string.contact_support_link))
                },
                onReportBugClick = {
                    context.startActivity(Intent(context, DynamicReportActivity::class.java))
                },
                onDebugLogsClick = {
                    context.startActivity(Intent(context, LogActivity::class.java))
                },
                onHelpFightClick = {
                    context.startActivity(Intent(context, SettingsTelemetryActivity::class.java))
                },
                onRateUsClick = {
                    val appPackageName = context.packageName
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
                    } catch (e: ActivityNotFoundException) {
                        // If market does not exist, open in browser
                        context.startActivity(AndroidUtils.playMarketIntentFor(appPackageName))
                    }
                },
                onThirdPartyLicensesClick = {
                    context.startActivity(Intent(context, OssLicensesActivity::class.java))
                }
            )
        }
    )

    val showReconnectDialogType = viewModel.showReconnectDialogFlow.collectAsStateWithLifecycle().value
    if (showReconnectDialogType != null) {
        ReconnectDialog(
            onOk = { notShowAgain -> viewModel.dismissReconnectDialog(notShowAgain, showReconnectDialogType) },
            onReconnect = { notShowAgain -> viewModel.onReconnectClicked(vpnUiDelegate, notShowAgain, showReconnectDialogType)
        })
    }
}

@Composable
fun ReconnectDialog(
    onOk: (notShowAgain: Boolean) -> Unit,
    onReconnect: (notShowAgain: Boolean) -> Unit
) {
    ProtonAlert(
        title = null,
        text = stringResource(id = R.string.settings_dialog_reconnect),
        checkBox = stringResource(id = R.string.dialogDontShowAgain),
        confirmLabel = stringResource(id = R.string.reconnect_now),
        onConfirm = { notShowAgain ->
            onReconnect(notShowAgain)
        },
        dismissLabel = stringResource(id = R.string.ok),
        onDismissButton = { notShowAgain ->
            onOk(notShowAgain)
        },
        checkBoxInitialValue = false,
        onDismissRequest = { onOk(false) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsibleToolbarScaffold(
    modifier: Modifier = Modifier,
    @StringRes titleResId: Int,
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val isCollapsed = remember { derivedStateOf { scrollBehavior.state.collapsedFraction > 0.5 } }

    val topAppBarElementColor = if (isCollapsed.value) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    val collapsedTextSize = 16
    val expandedTextSize = 28
    val topAppBarTextSize =
        (collapsedTextSize + (expandedTextSize - collapsedTextSize) * (1 - scrollBehavior.state.collapsedFraction)).sp

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = titleResId),
                        style = ProtonTheme.typography.defaultStrongNorm,
                        fontSize = topAppBarTextSize
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = ProtonTheme.colors.backgroundNorm,
                    scrolledContainerColor = ProtonTheme.colors.backgroundSecondary,
                    navigationIconContentColor = topAppBarElementColor,
                    titleContentColor = topAppBarElementColor,
                    actionIconContentColor = topAppBarElementColor,
                ),
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = contentWindowInsets,
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        content = content
    )
}

@Composable
private fun SettingsView(
    viewState: SettingsViewModel.SettingsViewState,
    settingsActions: SettingsActions,
    modifier: Modifier = Modifier
) {
    val userState = viewState.userInfo
    CollapsibleToolbarScaffold(
        modifier = modifier.windowInsetsPadding(WindowInsets.statusBars),
        titleResId = R.string.settings_title,
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            AccountCategory(
                userState = userState,
                onAccountClick = settingsActions.onAccountClick,
            )
            FeatureCategory(
                viewState = viewState,
                onNetShieldClick = settingsActions.onNetShieldClick,
                onNetShieldUpgrade = settingsActions.onNetShieldUpgradeClick,
                onSplitTunnelClick = settingsActions.onSplitTunnelClick,
                onSplitTunnelUpgrade = settingsActions.onSplitTunnelUpgrade,
                onAlwaysOnClick = settingsActions.onAlwaysOnClick
            )
            Category(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                stringResource(id = R.string.settings_connection_category)
            ) {
                SettingRowWithIcon(
                    icon = me.proton.core.auth.R.drawable.ic_proton_servers,
                    title = stringResource(id = viewState.protocol.titleRes),
                    onClick = settingsActions.onProtocolClick,
                    subtitle = stringResource(id = viewState.protocol.subtitleRes)
                )
                SettingRowWithIcon(
                    icon = viewState.vpnAccelerator.iconRes,
                    title = stringResource(id = viewState.vpnAccelerator.titleRes),
                    onClick = if (viewState.vpnAccelerator.isRestricted)
                        settingsActions.onVpnAcceleratorUpgrade else settingsActions.onVpnAcceleratorClick,
                    trailingIcon = viewState.vpnAccelerator.upgradeIconRes,
                    trailingIconTint = false,
                    subtitle = stringResource(id = viewState.vpnAccelerator.subtitleRes)
                )
                SettingRowWithIcon(
                    icon = me.proton.core.auth.R.drawable.ic_proton_sliders,
                    title = stringResource(id = R.string.settings_advanced_settings_title),
                    onClick = settingsActions.onAdvancedSettingsClick,
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Category(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                    stringResource(id = R.string.settings_category_general)
                ) {
                    SettingRowWithIcon(
                        icon = me.proton.core.auth.R.drawable.ic_proton_bell,
                        title = stringResource(id = R.string.settings_notifications_title),
                        onClick = settingsActions.onNotificationsClick,
                    )
                }
            }
            Category(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                stringResource(id = R.string.settings_category_support)
            ) {
                SettingRowWithIcon(
                    icon = me.proton.core.auth.R.drawable.ic_proton_life_ring,
                    title = stringResource(id = R.string.settings_help_center_title),
                    trailingIcon = me.proton.core.auth.R.drawable.ic_proton_arrow_out_square,
                    onClick = settingsActions.onOnHelpCenterClick,
                )
                SettingRowWithIcon(
                    icon = me.proton.core.auth.R.drawable.ic_proton_bug,
                    onClick = settingsActions.onReportBugClick,
                    title = stringResource(id = R.string.settings_report_issue_title)
                )
                SettingRowWithIcon(
                    icon = me.proton.core.auth.R.drawable.ic_proton_code,
                    onClick = settingsActions.onDebugLogsClick,
                    title = stringResource(id = R.string.settings_debug_logs_title)
                )
            }
            Category(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                stringResource(id = R.string.settings_category_improve_proton)
            ) {
                SettingRowWithIcon(
                    icon = me.proton.core.auth.R.drawable.ic_proton_users,
                    title = stringResource(id = R.string.settings_fight_censorship_title),
                    onClick = settingsActions.onHelpFightClick
                )
                SettingRowWithIcon(
                    icon = me.proton.core.auth.R.drawable.ic_proton_star,
                    title = stringResource(id = R.string.settings_rate_us_title),
                    trailingIcon = me.proton.core.auth.R.drawable.ic_proton_arrow_out_square,
                    onClick = settingsActions.onRateUsClick
                )

                Spacer(modifier = Modifier.size(8.dp))
            }
            SettingRowWithIcon(
                modifier = Modifier.padding(vertical = 8.dp),
                icon = CoreR.drawable.ic_proton_arrow_in_to_rectangle,
                title = stringResource(id = R.string.settings_sign_out),
                onClick = settingsActions.signOut
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { settingsActions.onThirdPartyLicensesClick() }
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(
                        id = R.string.settings_app_version,
                        BuildConfig.VERSION_NAME
                    ),
                    style = ProtonTheme.typography.captionWeak,
                )
                Text(
                    text = stringResource(id = R.string.settings_third_party_licenses),
                    color = ProtonTheme.colors.textAccent,
                    style = ProtonTheme.typography.captionNorm,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (viewState.buildInfo != null) {
                Text(
                    text = viewState.buildInfo,
                    style = ProtonTheme.typography.defaultSmallWeak,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeatureCategory(
    modifier: Modifier = Modifier,
    viewState: SettingsViewModel.SettingsViewState,
    onNetShieldClick: () -> Unit,
    onNetShieldUpgrade: () -> Unit,
    onSplitTunnelClick: () -> Unit,
    onSplitTunnelUpgrade: () -> Unit,
    onAlwaysOnClick: () -> Unit,
) {
    Category(
        modifier = modifier.padding(start = 16.dp, end = 16.dp),
        stringResource(id = R.string.settings_category_features)
    ) {
        SettingRowWithIcon(
            icon = viewState.netShield.iconRes,
            title = stringResource(id = viewState.netShield.titleRes),
            subtitle = stringResource(id = viewState.netShield.subtitleRes),
            trailingIcon = viewState.netShield.upgradeIconRes,
            trailingIconTint = false,
            onClick = if (viewState.netShield.isRestricted) onNetShieldUpgrade else onNetShieldClick
        )
        SettingRowWithIcon(
            icon = viewState.splitTunneling.iconRes,
            title = stringResource(id = viewState.splitTunneling.titleRes),
            subtitle = stringResource(id = viewState.splitTunneling.subtitleRes),
            trailingIcon = viewState.splitTunneling.upgradeIconRes,
            trailingIconTint = false,
            onClick = if (viewState.splitTunneling.isRestricted)
                onSplitTunnelUpgrade else onSplitTunnelClick
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SettingRowWithIcon(
                icon = R.drawable.ic_kill_switch,
                title = stringResource(id = R.string.settings_kill_switch_title),
                onClick = onAlwaysOnClick
            )
        }
    }
}

@Composable
private fun AccountCategory(
    modifier: Modifier = Modifier,
    userState: SettingsViewModel.UserViewState,
    onAccountClick: () -> Unit,
) {
    Category(
        modifier = modifier.padding(start = 16.dp, end = 16.dp),
        title = stringResource(id = R.string.settings_category_account)
    ) {
        SettingRowWithComposables(
            leadingComposable = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ProtonTheme.colors.brandNorm)
                        .padding(2.dp)
                ) {
                    Text(
                        text = userState.shortenedName,
                        style = ProtonTheme.typography.defaultSmallNorm
                    )
                }
            },
            title = userState.displayName,
            subtitle = userState.email,
            onClick = onAccountClick,
        )
    }
}

@Composable
private fun Category(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit
) {
    Text(
        text = title,
        color = ProtonTheme.colors.textAccent,
        style = ProtonTheme.typography.defaultSmallStrongUnspecified,
        modifier = modifier.padding(bottom = 8.dp, top = 16.dp)
    )
    content()
}

@Composable
private fun SettingRowWithComposables(
    modifier: Modifier = Modifier,
    leadingComposable: @Composable () -> Unit,
    trailingComposable: (@Composable () -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
) {
    var baseModifier = modifier
        .fillMaxWidth()

    if (onClick != null) {
        baseModifier = baseModifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    }
    baseModifier = baseModifier.padding(vertical = 16.dp, horizontal = 16.dp)

    Row(
        modifier = baseModifier,
        verticalAlignment = if (subtitle != null) Alignment.Top else Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(32.dp),
            contentAlignment = Alignment.Center
        ) {
            leadingComposable()
        }
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = ProtonTheme.typography.defaultNorm,
            )
            subtitle?.let {
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = it,
                    style = ProtonTheme.typography.defaultWeak
                )
            }
        }
        trailingComposable?.invoke()
    }
}

@Composable
fun SettingRowWithIcon(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    title: String,
    subtitle: String? = null,
    @DrawableRes trailingIcon: Int? = null,
    trailingIconTint: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    SettingRowWithComposables(
        leadingComposable = {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = Color.Unspecified,
            )
        },
        trailingComposable = {
            trailingIcon?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    tint = if (trailingIconTint) ProtonTheme.colors.iconWeak else Color.Unspecified,
                    modifier = Modifier
                        .padding(end = 8.dp)
                )
            }
        },
        title = title,
        subtitle = subtitle,
        onClick = onClick,
        modifier = modifier
    )
}

private data class SettingsActions(
    val onAccountClick: () -> Unit,
    val signOut: () -> Unit,
    val onNetShieldClick: () -> Unit,
    val onNetShieldUpgradeClick: () -> Unit,
    val onSplitTunnelClick: () -> Unit,
    val onSplitTunnelUpgrade: () -> Unit,
    val onAlwaysOnClick: () -> Unit,
    val onProtocolClick: () -> Unit,
    val onVpnAcceleratorClick: () -> Unit,
    val onVpnAcceleratorUpgrade: () -> Unit,
    val onAdvancedSettingsClick: () -> Unit,
    val onNotificationsClick: () -> Unit,
    val onOnHelpCenterClick: () -> Unit,
    val onReportBugClick: () -> Unit,
    val onDebugLogsClick: () -> Unit,
    val onHelpFightClick: () -> Unit,
    val onRateUsClick: () -> Unit,
    val onThirdPartyLicensesClick: () -> Unit,
)

@Preview
@Composable
fun SettingRowWithIconPreview() {
    SettingRowWithIcon(
        icon = R.drawable.vpn_plus_badge,
        title = "Netshield",
        subtitle = "On",
        onClick = { }
    )
}

@Preview
@Composable
fun SettingRowWithComposablesPreview() {
    SettingRowWithComposables(
        leadingComposable = {
            Text("A")
        },
        title = "User",
        subtitle = "user@mail.com",
        onClick = { }
    )
}

@Preview
@Composable
fun CategoryPreview() {
    Column {
        Category(title = stringResource(id = R.string.settings_category_features)) {
            SettingRowWithComposables(
                leadingComposable = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ProtonTheme.colors.brandNorm)
                    ) {
                        Text(
                            text = "AG",
                            style = ProtonTheme.typography.defaultNorm
                        )
                    }
                },
                title = stringResource(id = R.string.settings_netshield_title),
                subtitle = "On"
            )

            SettingRowWithIcon(
                icon = CoreR.drawable.ic_proton_earth,
                title = stringResource(id = R.string.settings_netshield_title),
                subtitle = "On"
            )
            SettingRowWithIcon(
                icon = CoreR.drawable.ic_proton_earth,
                title = stringResource(id = R.string.settings_split_tunneling_title),
                subtitle = "On"
            )
            SettingRowWithIcon(
                icon = CoreR.drawable.ic_proton_earth,
                title = stringResource(id = R.string.settings_kill_switch_title)
            )
        }
    }
}
