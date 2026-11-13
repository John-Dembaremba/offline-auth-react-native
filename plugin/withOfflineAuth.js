const { withAppBuildGradle } = require('expo/config-plugins');

const DESUGAR_DEPENDENCY = 'coreLibraryDesugaring "com.android.tools:desugar_jdk_libs:2.1.5"';

function configureDesugaring(contents) {
  let result = contents;

  // Enable core library desugaring in android.compileOptions.
  if (/coreLibraryDesugaringEnabled\s+false\b/.test(result)) {
    result = result.replace(
      /coreLibraryDesugaringEnabled\s+false\b/g,
      'coreLibraryDesugaringEnabled true'
    );
  } else if (!/coreLibraryDesugaringEnabled\s+true\b/.test(result)) {
    if (/\bcompileOptions\s*\{/.test(result)) {
      result = result.replace(
        /\bcompileOptions\s*\{/,
        'compileOptions {\n        coreLibraryDesugaringEnabled true'
      );
    } else if (/\bandroid\s*\{/.test(result)) {
      result = result.replace(
        /\bandroid\s*\{/,
        'android {\n    compileOptions {\n        coreLibraryDesugaringEnabled true\n    }'
      );
    } else {
      throw new Error('[offline-auth-react-native] Android Gradle block not found');
    }
  }

  // Add the desugaring dependency if it is not already configured.
  if (
    !/\bcoreLibraryDesugaring\s*(?:\(\s*|\s+)["']com\.android\.tools:desugar_jdk_libs:/.test(result)
  ) {
    if (!/\bdependencies\s*\{/.test(result)) {
      throw new Error('[offline-auth-react-native] Gradle dependencies block not found');
    }

    result = result.replace(/\bdependencies\s*\{/, `dependencies {\n    ${DESUGAR_DEPENDENCY}`);
  }

  return result;
}

const withOfflineAuth = (config) =>
  withAppBuildGradle(config, (config) => {
    if (config.modResults.language !== 'groovy') {
      throw new Error('[offline-auth-react-native] Only Groovy build.gradle is supported');
    }

    config.modResults.contents = configureDesugaring(config.modResults.contents);

    return config;
  });

module.exports = withOfflineAuth;
module.exports.configureDesugaring = configureDesugaring;
