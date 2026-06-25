package com.dhruva.allfirebasesampleapp.presentation.auth.additionalprovider

import android.app.Activity
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dhruva.allfirebasesampleapp.R

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun AdditionalProvidersScreen(
    viewModel: AdditionalProvidersViewModel = hiltViewModel(),
    onSignInSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AdditionalProviderItem(
            icon = R.drawable.ic_google,
            label = R.string.sign_in_with_google,
            onClick = {
                activity?.let {
                    viewModel.signInWithGoogle(
                        activity = it,
                        onSuccess = onSignInSuccess,
                        onFailure = { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        )

        AdditionalProviderItem(
            icon = R.drawable.ic_github,
            label = R.string.sign_in_with_github,
            onClick = {
                activity?.let {
                    viewModel.signInWithGithub(
                        activity = it,
                        onSuccess = onSignInSuccess,
                        onFailure = { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        )
    }
}

@Composable
fun AdditionalProviderItem(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    @StringRes label: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            modifier = Modifier
                .padding(8.dp)
                .size(120.dp, 200.dp)
                .align(Alignment.CenterHorizontally),
            painter = painterResource(icon),
            contentDescription = stringResource(label)
        )
        Button(
            onClick = onClick
        ) {
            Text(text = stringResource(label))
        }
    }
}
