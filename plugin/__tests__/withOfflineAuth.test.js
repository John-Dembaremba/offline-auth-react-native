const { configureDesugaring } = require('../withOfflineAuth');

const baseGradle = `
android {
    namespace 'com.example.app'
}

dependencies {
    implementation("com.facebook.react:react-android")
}
`;

describe('Expo Android config plugin', () => {
  test('adds desugaring to an unconfigured app', () => {
    const result = configureDesugaring(baseGradle);

    expect(result).toContain('coreLibraryDesugaringEnabled true');
    expect(result).toContain('coreLibraryDesugaring "com.android.tools:desugar_jdk_libs:2.1.5"');
  });

  test('does not duplicate existing configuration', () => {
    const gradle = `
android {
    compileOptions {
        coreLibraryDesugaringEnabled true
    }
}

dependencies {
    coreLibraryDesugaring "com.android.tools:desugar_jdk_libs:2.1.5"
}
`;

    const result = configureDesugaring(gradle);

    expect(result.match(/coreLibraryDesugaringEnabled true/g)).toHaveLength(1);

    expect(
      result.match(/coreLibraryDesugaring\s+["']com.android.tools:desugar_jdk_libs:/g)
    ).toHaveLength(1);
  });

  test('enables explicitly disabled desugaring', () => {
    const gradle = `
android {
    compileOptions {
        coreLibraryDesugaringEnabled false
    }
}

dependencies {
}
`;

    const result = configureDesugaring(gradle);

    expect(result).toContain('coreLibraryDesugaringEnabled true');
    expect(result).not.toContain('coreLibraryDesugaringEnabled false');
  });

  test('is idempotent', () => {
    const first = configureDesugaring(baseGradle);
    const second = configureDesugaring(first);

    expect(second).toBe(first);
  });

  test('rejects a missing android block', () => {
    expect(() => configureDesugaring('dependencies {}')).toThrow('Android Gradle block not found');
  });

  test('rejects a missing dependencies block', () => {
    expect(() => configureDesugaring('android {}')).toThrow('Gradle dependencies block not found');
  });
});
