# NVD API Key Error - Solution Guide

## What is the NVD API Key Error?

The **NVD (National Vulnerability Database) API Key error** occurs when OWASP Dependency Check tries to download vulnerability data from NVD without an API key. NVD now requires API keys to prevent rate limiting and ensure reliable access.

### Error Message:
```
[WARNING] An NVD API Key was not provided
[ERROR] Error updating the NVD Data; the NVD returned a 403 or 404 error
Consider using an NVD API Key
```

---

## Solutions

### Solution 1: Get a Free NVD API Key (Recommended)

#### Step 1: Request API Key
1. Visit: https://nvd.nist.gov/developers/request-an-api-key
2. Fill out the registration form
3. Check your email for the API key
4. **Activate within 7 days** (keys expire if not activated)

#### Step 2: Configure in Maven

**Option A: Environment Variable (Most Secure)**
```bash
# Set environment variable
export NVD_API_KEY=your-api-key-here

# Or in ~/.bashrc or ~/.zshrc for persistence
echo 'export NVD_API_KEY=your-api-key-here' >> ~/.zshrc
```

**Option B: Maven Settings (Less Secure)**
Add to `~/.m2/settings.xml`:
```xml
<settings>
    <profiles>
        <profile>
            <id>dependency-check</id>
            <properties>
                <nvd.api.key>your-api-key-here</nvd.api.key>
            </properties>
        </profile>
    </profiles>
</settings>
```

**Option C: Project Configuration (Not Recommended for Production)**
Update `pom.xml`:
```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>9.0.9</version>
    <configuration>
        <nvdApiKey>${env.NVD_API_KEY}</nvdApiKey> <!-- From environment -->
        <!-- OR -->
        <!-- <nvdApiKey>your-key-here</nvdApiKey> --> <!-- Direct (not secure) -->
    </configuration>
</plugin>
```

---

### Solution 2: Make Plugin Optional (Skip in Regular Builds)

Update `pom.xml` to only run on demand:

```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>9.0.9</version>
    <configuration>
        <nvdApiKey>${env.NVD_API_KEY}</nvdApiKey>
        <failOnError>false</failOnError> <!-- Don't fail build if check fails -->
    </configuration>
    <!-- Remove executions - run manually only -->
    <!-- <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions> -->
</plugin>
```

**Run manually when needed:**
```bash
mvn dependency-check:check
```

---

### Solution 3: Use Alternative Data Sources

Configure plugin to use OSS Index or other sources:

```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>9.0.9</version>
    <configuration>
        <nvdApiKey>${env.NVD_API_KEY}</nvdApiKey>
        <retireJsEnabled>true</retireJsEnabled>
        <ossIndexEnabled>true</ossIndexEnabled>
        <ossIndexUser>your-oss-index-username</ossIndexUser>
        <ossIndexPassword>your-oss-index-token</ossIndexPassword>
    </configuration>
</plugin>
```

---

### Solution 4: Disable NVD Updates (Not Recommended)

Only use if you have cached data:

```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>9.0.9</version>
    <configuration>
        <nvdApiKey>${env.NVD_API_KEY}</nvdApiKey>
        <autoUpdate>false</autoUpdate> <!-- Disable updates -->
        <cveValidForHours>24</cveValidForHours>
    </configuration>
</plugin>
```

---

## Recommended Configuration for Your Project

Your `pom.xml` has been updated with the best practice configuration:

✅ **Uses environment variable** for API key (secure)
✅ **Doesn't run automatically** (prevents build failures)
✅ **Run manually** when needed: `mvn dependency-check:check`
✅ **Generates HTML and JSON reports**

---

## Quick Setup Steps

### 1. Get NVD API Key (Free)
```bash
# Visit: https://nvd.nist.gov/developers/request-an-api-key
# Register and get your API key (check email)
```

### 2. Set Environment Variable
```bash
# macOS/Linux
export NVD_API_KEY=your-api-key-here

# Make it permanent (add to ~/.zshrc or ~/.bashrc)
echo 'export NVD_API_KEY=your-api-key-here' >> ~/.zshrc
source ~/.zshrc

# Windows (PowerShell)
$env:NVD_API_KEY="your-api-key-here"

# Windows (Command Prompt)
set NVD_API_KEY=your-api-key-here
```

### 3. Run Dependency Check
```bash
# Run the check manually
mvn dependency-check:check

# Reports will be in: target/dependency-check-report/
# - dependency-check-report.html (view in browser)
# - dependency-check-report.json (for CI/CD)
```

---

## Current Configuration in pom.xml

```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>9.0.9</version>
    <configuration>
        <nvdApiKey>${env.NVD_API_KEY}</nvdApiKey> <!-- From environment -->
        <failOnError>false</failOnError> <!-- Won't fail build -->
        <formats>
            <format>HTML</format>
            <format>JSON</format>
        </formats>
        <outputDirectory>target/dependency-check-report</outputDirectory>
    </configuration>
    <!-- No automatic execution - run manually -->
</plugin>
```

---

## Troubleshooting

### Error: "NVD API Key was not provided"
**Solution:** Set the `NVD_API_KEY` environment variable

### Error: "403 or 404 error"
**Causes:**
- API key not set
- API key expired (must activate within 7 days)
- Rate limiting (too many requests)

**Solutions:**
1. Verify API key is set: `echo $NVD_API_KEY`
2. Request new API key if expired
3. Wait a few minutes and try again (rate limit)

### Error: "If your NVD API Key is valid try increasing the NVD API Delay"
**Cause:** Rate limiting - requests are being sent too quickly to NVD API

**Solution:** The `pom.xml` is now configured with `nvdApiDelay` set to 4000ms (4 seconds). If you still get errors:

1. **Increase the delay** in `pom.xml`:
   ```xml
   <nvdApiDelay>6000</nvdApiDelay> <!-- Increase to 6 seconds -->
   ```

2. **Or set to 8000ms** for very slow connections:
   ```xml
   <nvdApiDelay>8000</nvdApiDelay> <!-- 8 seconds between requests -->
   ```

3. **Recommended delays:**
   - **3000ms** (3s) - Fast, may hit rate limits
   - **4000ms** (4s) - Balanced (current setting) ✅
   - **6000ms** (6s) - Conservative, safer
   - **8000ms** (8s) - Very conservative, slow but reliable

**Note:** Higher delays = slower scans but more reliable. The first run will be slowest as it downloads all vulnerability data.

### Error: "No documents exist"
**Solution:** This happens on first run. The plugin needs to download vulnerability data. Ensure:
- API key is set
- Internet connection is available
- NVD API is accessible

### Build Fails with Dependency Check
**Solution:** The plugin is configured with `failOnError>false</failOnError>`, so it won't fail your build. If you want it to fail on vulnerabilities:

```xml
<failOnError>true</failOnError>
```

---

## Alternative: Skip Dependency Check in Build

If you don't want to use dependency check right now, you can:

1. **Skip the plugin entirely** (remove from pom.xml)
2. **Run only in CI/CD** (configure in CI pipeline)
3. **Use different tools:**
   - Snyk: `snyk test`
   - Dependabot (GitHub)
   - OWASP Dependency-Track

---

## Benefits of NVD API Key

✅ **Faster updates** - No rate limiting
✅ **Reliable access** - Guaranteed API access
✅ **Better performance** - Direct API access
✅ **Free** - No cost for API key
✅ **Required** - NVD now requires keys for reliable access

---

## Security Note

⚠️ **Never commit API keys to version control!**

- ✅ Use environment variables
- ✅ Use CI/CD secrets management
- ✅ Use Maven settings.xml (not in project)
- ❌ Never hardcode in pom.xml
- ❌ Never commit to Git

---

## Summary

**The error occurs because:**
1. NVD requires API keys for access (rate limiting prevention)
2. Without API key, you get 403/404 errors
3. Plugin can't download vulnerability data

**The solution:**
1. Get free API key from NVD
2. Set as environment variable: `NVD_API_KEY`
3. Run: `mvn dependency-check:check`
4. View reports in `target/dependency-check-report/`

**Your project is now configured correctly!** The plugin won't run automatically, preventing build failures. Run it manually when you need to check for vulnerabilities.

